# Refer 'https://github.com/DavidAnson/markdownlint' of how to suppress the markdown rules.
param (
  # url list to verify links. Can either be a http address or a local file request. Local file paths support md and html files.
  [string[]] $urls,
  # file that contains a set of links to ignore when verifying
  [string] $ignoreMarkdownFile = "$PSScriptRoot/ignore-links.txt",
  # switch that will enable devops specific logging for warnings
  [switch] $devOpsLogging = $false
)

function LogWarning
{
  if ($devOpsLogging)
  {
    Write-Host "##vso[task.LogIssue type=warning;]$args"
  }
  else
  {
    Write-Warning "$args"
  }
}

if ($urls) {
    if ($urls.Count -eq 0) {
        Write-Host "Usage $($MyInvocation.MyCommand.Name) <urls>";
        exit 1;
    }  
}

$ignoreLinks = @();
if (Test-Path $ignoreLinksFile)
{
  $ignoreLinks = [Array](Get-Content $ignoreLinksFile | ForEach-Object { ($_ -replace "#.*", "").Trim() } | Where-Object { $_ -ne "" })
}

foreach  ($url in $urls) {
    if ($ignoreLinks.Contains($url)) {
        continue
    }
    try {
        markdownlint $url --ignore node_modules
    }
    catch {
        LogWarning $_.Exception.ToString()
    }
}