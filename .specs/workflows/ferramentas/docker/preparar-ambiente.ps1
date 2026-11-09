<#
 Prepara o .env para subir o compose sem conflito de porta.
 - cria o .env a partir do .env.example se não existir;
 - para cada VAR=padrão de -Portas: mantém a porta se estiver livre (ou se for de um contêiner DESTE projeto)
   e, se estiver ocupada por outro processo/contêiner, troca pela próxima livre.
 Uso: ./preparar-ambiente.ps1 -Projeto meu-projeto -Portas 'POSTGRES_PORT=5432,API_PORT=8080,APP_PORT=4200'
#>
param(
  [string]$Projeto = '',
  [Parameter(Mandatory)] [string]$Portas,
  [string]$ArquivoEnv = '.env',
  [string]$Exemplo = '.env.example'
)
$ErrorActionPreference = 'Stop'

if (-not (Test-Path $ArquivoEnv)) {
  if (-not (Test-Path $Exemplo)) { throw "Nem $ArquivoEnv nem $Exemplo existem." }
  Copy-Item $Exemplo $ArquivoEnv
  Write-Host "Criado $ArquivoEnv a partir de $Exemplo" -ForegroundColor Green
}
$linhas = [System.Collections.Generic.List[string]]::new()
foreach ($l in (Get-Content $ArquivoEnv)) { $linhas.Add($l) }

# portas em uso na máquina
$ocupadas = [System.Collections.Generic.HashSet[int]]::new()
try {
  Get-NetTCPConnection -State Listen -ErrorAction Stop | ForEach-Object { [void]$ocupadas.Add([int]$_.LocalPort) }
} catch {
  netstat -ano | Select-String 'LISTENING' | ForEach-Object { if ($_ -match ':(\d+)\s') { [void]$ocupadas.Add([int]$Matches[1]) } }
}

# portas dos contêineres DESTE projeto não contam como conflito (o compose as reutiliza)
$proprias = [System.Collections.Generic.HashSet[int]]::new()
if ($Projeto -and (Get-Command docker -ErrorAction SilentlyContinue)) {
  # `compose -p` lista só os contêineres deste projeto (rótulos de imagem podem ter quebra de linha: não use {{.Labels}}).
  foreach ($linha in (docker compose -p $Projeto ps --format '{{.Ports}}' 2>$null)) {
    foreach ($m in [regex]::Matches([string]$linha, ':(\d+)->')) { [void]$proprias.Add([int]$m.Groups[1].Value) }
  }
}

foreach ($par in ($Portas -split ',')) {
  $nome, $padrao = $par.Trim() -split '=', 2
  $idx = -1
  for ($i = 0; $i -lt $linhas.Count; $i++) { if ($linhas[$i] -match "^\s*$([regex]::Escape($nome))\s*=") { $idx = $i; break } }
  $atual = if ($idx -ge 0) { [int](($linhas[$idx] -split '=', 2)[1].Trim()) } else { [int]$padrao }
  $porta = $atual
  while ($ocupadas.Contains($porta) -and -not $proprias.Contains($porta)) { $porta++ }
  [void]$ocupadas.Add($porta)
  if ($porta -ne $atual) { Write-Host "$nome=$atual está ocupada -> usando $porta" -ForegroundColor Yellow }
  if ($idx -ge 0) { $linhas[$idx] = "$nome=$porta" } else { $linhas.Add("$nome=$porta") }
}
Set-Content $ArquivoEnv $linhas -Encoding utf8
Write-Host 'Portas do host:' -ForegroundColor Cyan
Get-Content $ArquivoEnv | Select-String -Pattern '_PORT=' | ForEach-Object { "  $($_.Line)" }
