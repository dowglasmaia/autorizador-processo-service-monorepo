# Sobe tudo (PostgreSQL + API + frontend) com um comando: prepara o .env com portas livres,
# constrói as imagens, espera ficar saudável e mostra as URLs. Pode ser chamado de qualquer pasta.
$ErrorActionPreference = 'Stop'
Set-Location (Resolve-Path "$PSScriptRoot/../../../..")   # raiz do projeto (onde ficam compose.yaml e .env)
& "$PSScriptRoot/preparar-ambiente.ps1" -Projeto 'autorizador-processo' -Portas 'POSTGRES_PORT=5432,API_PORT=8080,APP_PORT=4200'
docker compose up -d --build --wait
if ($LASTEXITCODE -ne 0) { docker compose ps; Write-Host 'Falhou: veja docker compose logs <servico>' -ForegroundColor Red; exit 1 }
docker compose ps
$e = @{}; Get-Content .env | ForEach-Object { if ($_ -match '^(\w+)=(.*)$') { $e[$Matches[1]] = $Matches[2] } }
Write-Host "`nFrontend:  http://localhost:$($e['APP_PORT'])"
Write-Host "API:       http://localhost:$($e['API_PORT'])   (readiness: /actuator/health/readiness)"
Write-Host "PostgreSQL: localhost:$($e['POSTGRES_PORT'])"
