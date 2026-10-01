<# : batch portion
@REM ----------------------------------------------------------------------------
@REM Licensed to the Apache Software Foundation (ASF) under one
@REM or more contributor license agreements.  See the NOTICE file
@REM distributed with this work for additional information
@REM regarding copyright ownership.  The ASF licenses this file
@REM to you under the Apache License, Version 2.0 (the
@REM "License"); you may not use this file except in compliance
@REM with the License.  You may obtain a copy of the License at
@REM
@REM    http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing,
@REM software distributed under the License is distributed on an
@REM "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
@REM KIND, either express or implied.  See the License for the
@REM specific language governing permissions and limitations
@REM under the License.
@REM ----------------------------------------------------------------------------

@REM ----------------------------------------------------------------------------
@REM Apache Maven Wrapper startup batch script, version 3.3.4
@REM
@REM Optional ENV vars
@REM   MVNW_REPOURL - repo url base for downloading maven distribution
@REM   MVNW_USERNAME/MVNW_PASSWORD - user and password for downloading maven
@REM   MVNW_VERBOSE - true: enable verbose log; others: silence the output
@REM ----------------------------------------------------------------------------

@IF "%__MVNW_ARG0_NAME__%"=="" (SET __MVNW_ARG0_NAME__=%~nx0)
@SET __MVNW_CMD__=
@SET __MVNW_ERROR__=
@SET __MVNW_PSMODULEP_SAVE=%PSModulePath%
@SET PSModulePath=
@FOR /F "usebackq tokens=1* delims==" %%A IN (`powershell -noprofile "& {$scriptDir='%~dp0'; $script='%__MVNW_ARG0_NAME__%'; & ([Scriptblock]::Create((Get-Content -Raw '%~f0')))}"`) DO @(
  IF "%%A"=="MVN_CMD" (set __MVNW_CMD__=%%B) ELSE IF "%%B"=="" (echo %%A) ELSE (echo %%A=%%B)
)
@SET PSModulePath=%__MVNW_PSMODULEP_SAVE%
@SET __MVNW_PSMODULEP_SAVE=
@SET __MVNW_ARG0_NAME__=
@SET MVNW_USERNAME=
@SET MVNW_PASSWORD=
@IF "%__MVNW_CMD__%"=="" GOTO :MVNW_ERROR
@SET "MAVEN_OPTS=-Duser.home=%USERPROFILE% %MAVEN_OPTS%"
@CALL "%__MVNW_CMD__%" %*
@EXIT /B %ERRORLEVEL%
:MVNW_ERROR
@echo Cannot start maven from wrapper >&2
@EXIT /B 1
: end batch / begin powershell #>
$ErrorActionPreference = "Stop"
if ($env:MVNW_VERBOSE -eq "true") { $VerbosePreference = "Continue" }

$propertiesPath = Join-Path $scriptDir ".mvn/wrapper/maven-wrapper.properties"
$properties = Get-Content -Raw $propertiesPath | ConvertFrom-StringData
$distributionUrl = $properties.distributionUrl
if ($env:MVNW_REPOURL) {
  $distributionUrl = "$env:MVNW_REPOURL/org/apache/maven/$($distributionUrl -replace '^.*/org/apache/maven/','')"
}
$distributionUrlName = [System.IO.Path]::GetFileName(($distributionUrl -split '\?')[0])
$distributionName = $distributionUrlName -replace '-bin\.zip$',''
if ($distributionUrlName -notmatch '-bin\.zip$') { throw "Maven distribution URL must end with -bin.zip: $distributionUrl" }
$MVN_CMD = $script -replace '^mvnw','mvn'
$mavenUserHome = if ($env:MAVEN_USER_HOME) { $env:MAVEN_USER_HOME } else { Join-Path $env:USERPROFILE '.m2' }
$distributionRoot = Join-Path (Join-Path (Join-Path $mavenUserHome 'wrapper/dists') ($distributionName + '-bin')) ''

$cached = Get-ChildItem -Path $distributionRoot -Directory -Recurse -ErrorAction SilentlyContinue |
  Where-Object { Test-Path (Join-Path (Join-Path $_.FullName $distributionName) "bin/$MVN_CMD") } |
  Select-Object -First 1
if ($cached) {
  Write-Output "MVN_CMD=$(Join-Path (Join-Path $cached.FullName $distributionName) "bin/$MVN_CMD")"
  exit 0
}

$hash = [Convert]::ToHexString([System.Security.Cryptography.SHA256]::HashData([System.Text.Encoding]::UTF8.GetBytes($distributionUrl))).ToLowerInvariant()
$hashDirectory = Join-Path $distributionRoot $hash
New-Item -ItemType Directory -Path $hashDirectory -Force | Out-Null
$archive = Join-Path $hashDirectory $distributionUrlName
if (-not (Test-Path $archive)) {
  Write-Verbose "Downloading Maven from $distributionUrl"
  $client = [System.Net.WebClient]::new()
  if ($env:MVNW_USERNAME -and $env:MVNW_PASSWORD) { $client.Credentials = [System.Net.NetworkCredential]::new($env:MVNW_USERNAME,$env:MVNW_PASSWORD) }
  $client.DownloadFile($distributionUrl,$archive)
}
if ($properties.distributionSha256Sum) {
  $actual = (Get-FileHash -LiteralPath $archive -Algorithm SHA256).Hash.ToLowerInvariant()
  if ($actual -ne $properties.distributionSha256Sum.ToLowerInvariant()) { throw 'Maven distribution SHA-256 checksum validation failed.' }
}
Expand-Archive -LiteralPath $archive -DestinationPath $hashDirectory -Force
$mavenCommand = Join-Path (Join-Path $hashDirectory $distributionName) "bin/$MVN_CMD"
if (-not (Test-Path $mavenCommand -PathType Leaf)) { throw 'Maven executable was not found in the downloaded distribution.' }
Write-Output "MVN_CMD=$mavenCommand"
