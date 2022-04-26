$Language = "android"
$LanguageDisplayName = "Android"
$PackageRepository = "Maven"
$packagePattern = "*.pom"
$MetadataUri = "https://raw.githubusercontent.com/Azure/azure-sdk/main/_data/releases/latest/android-packages.csv"
$packageDownloadUrl = "https://repo1.maven.org/maven2"

function Get-AllPackageInfoFromRepo ($serviceDirectory)
{
  $allPackageProps = @()
  Push-Location $RepoRoot

  if ($serviceDirectory)
  {
    $properties = gradle ":sdk:${serviceDirectory}:retrieveProjectProperties" -q
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

