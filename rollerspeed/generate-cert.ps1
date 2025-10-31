$password = ConvertTo-SecureString -String "rollerspeed" -Force -AsPlainText

$cert = New-SelfSignedCertificate `
    -Subject "CN=localhost" `
    -DnsName "localhost" `
    -KeyAlgorithm RSA `
    -KeyLength 2048 `
    -NotBefore (Get-Date) `
    -NotAfter (Get-Date).AddYears(1) `
    -CertStoreLocation "Cert:\CurrentUser\My" `
    -FriendlyName "Rollerspeed SSL Certificate" `
    -HashAlgorithm SHA256 `
    -KeyUsage DigitalSignature, KeyEncipherment, DataEncipherment `
    -TextExtension @("2.5.29.37={text}1.3.6.1.5.5.7.3.1")

$pfxPath = ".\rollerspeed.pfx"
$certPath = "Cert:\CurrentUser\My\$($cert.Thumbprint)"

Export-PfxCertificate -Cert $certPath -FilePath $pfxPath -Password $password

Write-Host "Certificado creado exitosamente en: $pfxPath"
Write-Host "La contraseña del certificado es: rollerspeed"