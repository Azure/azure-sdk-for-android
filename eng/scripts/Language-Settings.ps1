$Language = "android"
$LanguageDisplayName = "Android"
$PackageRepository = "Maven"
$packagePattern = "*.pom"

$MetadataUri = "https://raw.githubusercontent.com/Azure/azure-sdk/main/_data/releases/latest/android-packages.csv"
$BlobStorageUrl = "https://azuresdkdocs.blob.core.windows.net/%24web?restype=container&comp=list&prefix=android%2F&delimiter=%2F"
$CampaignTag = Resolve-Path (Join-Path -Path $PSScriptRoot -ChildPath "../repo-docs/ga_tag.html")
$GithubUri = "https://github.com/Azure/azure-sdk-for-android"
$PackageRepositoryUri = "https://repo1.maven.org/maven2"

function Get-AllPackageInfoFromRepo ($serviceDirectory)
{
  $allPackageProps = @()
  Push-Location $RepoRoot

  if ($serviceDirectory)
  {
    $properties = gradle ":sdk:$serviceDirectory:retrieveProjectProperties" -q
  }
  else
  {
    $properties = gradle retrieveProjectProperties -q
  }
  Pop-Location
  foreach($line in $properties)
  {
    if ($line -match "^(?<version>.*)?~(?<name>.*)?~(?<group>.*)~(?<projDir>.*)")
    {
      $pkgName = $Matches["name"]
      $pkgVersion = $Matches["version"]
      $pkgGroup = $Matches["group"]
      $pkgPath = $Matches["projDir"]
      $serviceDirectory = Split-Path -Path $pkgPath -Parent
      $serviceDirName = Split-Path -Path $serviceDirectory -Leaf

      $pkgProp = [PackageProps]::new($pkgName, $pkgVersion, $pkgPath, $serviceDirName, $pkgGroup)
      $allPackageProps += $pkgProp
    }

  }
  return $allPackageProps
}

function SetPackageVersion ($PackageName, $Version, $ReleaseDate, $ReplaceLatestEntryTitle=$true)
{
  if($null -eq $ReleaseDate)
  {
    $ReleaseDate = Get-Date -Format "yyyy-MM-dd"
  }
  & "$EngDir/scripts/Update-PkgVersion.ps1" -PackageName $PackageName -NewVersionString $Version `
  -ReleaseDate $ReleaseDate -ReplaceLatestEntryTitle $ReplaceLatestEntryTitle
}

# Returns the maven (really sonatype) publish status of a package id and version.
function IsMavenPackageVersionPublished($pkgId, $pkgVersion, $groupId)
{
  $uri = "https://oss.sonatype.org/content/repositories/releases/$($groupId.Replace('.', '/'))/$pkgId/$pkgVersion/$pkgId-$pkgVersion.pom"

  $attempt = 1
  while ($attempt -le 3)
  {
    try
    {
      if ($attempt -gt 1) {
        Start-Sleep -Seconds ([Math]::Pow(2, $attempt))
      }

      Write-Host "Checking published package at $uri"
      $response = Invoke-WebRequest -Method "GET" -uri $uri -SkipHttpErrorCheck

      if ($response.BaseResponse.IsSuccessStatusCode)
      {
        return $true
      }

      $statusCode = $response.StatusCode

      if ($statusCode -eq 404)
      {
        return $false
      }

      Write-Host "Http request for maven package $groupId`:$pkgId`:$pkgVersion failed attempt $attempt with statuscode $statusCode"
    }
    catch
    {
      Write-Host "Http request for maven package $groupId`:$pkgId`:$pkgVersion failed attempt $attempt with exception $($_.Exception.Message)"
    }

    $attempt += 1
  }

  throw "Http request for maven package $groupId`:$pkgId`:$pkgVersion failed after 3 attempts"
}

# Parse out package publishing information given a maven POM file
function Get-android-PackageInfoFromPackageFile ($pkg, $workingDirectory)
{
  [xml]$contentXML = Get-Content $pkg

  $pkgId = $contentXML.project.artifactId
  $docsReadMeName = $pkgId -replace "^azure-" , ""
  $pkgVersion = $contentXML.project.version
  $groupId = if ($contentXML.project.groupId -eq $null) { $contentXML.project.parent.groupId } else { $contentXML.project.groupId }
  $releaseNotes = ""
  $readmeContent = ""

  # if it's a snapshot. return $null (as we don't want to create tags for this, but we also don't want to fail)
  if ($pkgVersion.Contains("SNAPSHOT")) {
    return $null
  }

  $changeLogLoc = @(Get-ChildItem -Path $pkg.DirectoryName -Recurse -Include "$($pkg.Basename)-changelog.md")[0]
  if ($changeLogLoc) {
    $releaseNotes = Get-ChangeLogEntryAsString -ChangeLogLocation $changeLogLoc -VersionString $pkgVersion
  }

  $readmeContentLoc = @(Get-ChildItem -Path $pkg.DirectoryName -Recurse -Include "$($pkg.Basename)-readme.md")[0]
  if ($readmeContentLoc) {
    $readmeContent = Get-Content -Raw $readmeContentLoc
  }

  return New-Object PSObject -Property @{
    PackageId      = $pkgId
    GroupId        = $groupId
    PackageVersion = $pkgVersion
    ReleaseTag     = "$($pkgId)_$($pkgVersion)"
    Deployable     = $forceCreate -or !(IsMavenPackageVersionPublished -pkgId $pkgId -pkgVersion $pkgVersion -groupId $groupId.Replace(".", "/"))
    ReleaseNotes   = $releaseNotes
    ReadmeContent  = $readmeContent
    DocsReadMeName = $docsReadMeName
  }
}

function Publish-android-GithubIODocs ($DocLocation, $PublicArtifactLocation)
{
  $PublishedDocs = Get-ChildItem "$DocLocation" | Where-Object -FilterScript {$_.Name.EndsWith("-javadoc.jar")}
  foreach ($Item in $PublishedDocs)
  {
    $UnjarredDocumentationPath = ""
    try
    {
      $PkgName = $Item.BaseName
      # The jar's unpacking command doesn't allow specifying a target directory
      # and will unjar all of the files in whatever the current directory is.
      # Create a subdirectory to unjar into, set the location, unjar and then
      # set the location back to its original location.
      $UnjarredDocumentationPath = Join-Path -Path $DocLocation -ChildPath $PkgName
      New-Item -ItemType directory -Path "$UnjarredDocumentationPath"
      $CurrentLocation = Get-Location
      Set-Location $UnjarredDocumentationPath
      jar -xf "$($Item.FullName)"
      Set-Location $CurrentLocation

      # If javadocs are produced for a library with source, there will always be an
      # index.html. If this file doesn't exist in the UnjarredDocumentationPath then
      # this is a sourceless library which means there are no javadocs and nothing
      # should be uploaded to blob storage.
      $IndexHtml = Join-Path -Path $UnjarredDocumentationPath -ChildPath "index.html"
      if (!(Test-Path -path $IndexHtml))
      {
        Write-Host "$($PkgName) does not have an index.html file, skipping."
        continue
      }

      # Get the POM file for the artifact we're processing
      $PomFile = $Item.FullName.Substring(0,$Item.FullName.LastIndexOf(("-javadoc.jar"))) + ".pom"
      Write-Host "PomFile $($PomFile)"

      # Pull the version from the POM
      [xml]$PomXml = Get-Content $PomFile
      $Version = $PomXml.project.version
      $ArtifactId = $PomXml.project.artifactId

      # inject the ga tag just before we upload the index to storage.
      $indexContent = Get-Content -Path $IndexHtml -Raw
      $tagContent = Get-Content -Path $CampaignTag -Raw

      $indexContent = $indexContent.Replace("</head>", $tagContent + "</head>")
      Set-Content -Path $IndexHtml -Value $indexContent -NoNewline

      Write-Host "Start Upload for $($PkgName)/$($Version)"
      Write-Host "DocDir $($UnjarredDocumentationPath)"
      Write-Host "PkgName $($ArtifactId)"
      Write-Host "DocVersion $($Version)"
      $releaseTag = RetrieveReleaseTag $PublicArtifactLocation
      Write-Host "releaseTag $($releaseTag)"
      Upload-Blobs -DocDir $UnjarredDocumentationPath -PkgName $ArtifactId -DocVersion $Version -ReleaseTag $releaseTag
    }
    Finally
    {
      if (![string]::IsNullOrEmpty($UnjarredDocumentationPath))
      {
        if (Test-Path -Path $UnjarredDocumentationPath)
        {
          Write-Host "Cleaning up $UnjarredDocumentationPath"
          Remove-Item -Recurse -Force $UnjarredDocumentationPath
        }
      }
    }
  }
}