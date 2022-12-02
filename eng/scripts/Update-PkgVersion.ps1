[CmdletBinding()]
Param (
  [Parameter(Mandatory=$True)]
  [string] $PackageName,
  [string] $NewVersionString,
  [string] $ReleaseDate,
  [boolean] $ReplaceLatestEntryTitle=$true
)

. (Join-Path $PSScriptRoot ".." common scripts common.ps1)

$pkgProperties = Get-PkgProperties -PackageName $PackageName
$packageVersion = $pkgProperties.Version

$packageSemVer = [AzureEngSemanticVersion]::new($packageVersion)

if ([System.String]::IsNullOrEmpty($NewVersionString))
{
    $packageSemVer.IncrementAndSetToPrerelease()
    & "${EngCommonScriptsDir}/Update-ChangeLog.ps1" -Version $packageSemVer.ToString() `
    -ChangelogPath $pkgProperties.ChangeLogPath -Unreleased $True
}
else
{
    $packageSemVer = [AzureEngSemanticVersion]::new($NewVersionString)
    & "${EngCommonScriptsDir}/Update-ChangeLog.ps1" -Version $packageSemVer.ToString() `
    -ChangelogPath $pkgProperties.ChangeLogPath -Unreleased $False `
    -ReplaceLatestEntryTitle $ReplaceLatestEntryTitle -ReleaseDate $ReleaseDate
}

gradle ":sdk:$($pkgProperties.ServiceDirectory):$($pkgProperties.Name):updatePackageVersion" -q -PnewVersion="$($packageSemVer.ToString())"