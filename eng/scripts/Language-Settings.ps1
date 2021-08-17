$Language = "android"

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