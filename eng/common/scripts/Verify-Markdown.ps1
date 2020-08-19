# Refer 'https://github.com/DavidAnson/markdownlint' of how to suppress the markdown rules.
param (
  # url list to verify links. Can either be a http address or a local file request. Local file paths support md and html files.
  [string[]] $urls,
  # file that contains a set of links to ignore when verifying
  [string] $ignoreMarkdownFile = "$PSScriptRoot/ignore-markdowns.txt",
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
# Install the markdown lint package. The command is limited to linux. 
# TODO: Fix command if it applies to other operating system.
try {
  sudo npm install -g markdownlint-cli
} 
catch {
  Write-Error "Something goes wrong with npm server."
}

if ($urls) {
  if ($urls.Count -eq 0) {
    Write-Host "Usage $($MyInvocation.MyCommand.Name) <urls>";
    exit 1;
  }  
}

$ignoreLinks = @();
Write-Host "1.Scaning markdown file ($ignoreMarkdownFile)"
if (Test-Path $ignoreMarkdownFile)
{
  $ignoreLinks = [Array](Get-Content $ignoreMarkdownFile | ForEach-Object { ($_ -replace "#.*", "").Trim() } | Where-Object { $_ -ne "" })
}
Write-Host "2.Scaning markdown file ($ignoreLinks)"
foreach  ($url in $urls) {
  
  if ($ignoreLinks.Contains($url)) {
    continue
  }
  try {
    Write-Verbose "Scaning markdown file ($url)"
    markdownlint $url
  }
  catch {
    LogWarning $_.Exception.ToString()
  }
}