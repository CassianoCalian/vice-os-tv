# VICE OS 1.0 — Android TV HQ

Launcher experimental para Android TV, tema Vice City, interface de navegação por controle remoto. Não altera firmware.

## Compilar no GitHub

1. Envie o conteúdo deste ZIP (arquivos na raiz) para o repositório GitHub.
2. Abra Actions > Gerar APK VICE OS > Run workflow.
3. Baixe o artifact VICE-OS-1.0-APK, extraia app-debug.apk.
4. Envie app-debug.apk com Send Files to TV e instale pela TV.
5. Abra VICE OS na lista de apps; se a TV oferecer escolha de app de início, teste depois de verificar o funcionamento.

## Segurança e limitações

É um primeiro release de testes; não há integração real com histórico de streaming. O launcher pode não ser selecionável como HOME em firmware HQ. Para voltar, use o botão Home original ou Configurações > Apps > VICE OS > Desinstalar. Não desative o launcher HQ via ADB.

## Build local opcional

Abra a pasta no Android Studio com JDK 17 e SDK Android 35 e execute `gradle :app:assembleDebug` (ou use a opção Build APK).
