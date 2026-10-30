# CALIAN TV 1.0 — Android TV HQ

Launcher Android TV inspirado no layout cinematográfico selecionado pelo usuário. A interface contém menu lateral, banner com movimento suave, faixas de apps instalados, destaques e canais visuais e navegação pelo controle remoto.

## Recursos funcionais

- O aplicativo se chama **CALIAN TV**, mas mantém o ID `com.viceos.tv` para atualizar a versão anterior.
- Aplicativos são detectados a partir dos intents `LEANBACK_LAUNCHER` e `LAUNCHER`, mostram seus ícones nativos e abrem pela ação de lançamento do Android.
- Atalhos das seções de filmes, TV e canais **dependem dos aplicativos que estão instalados**. O launcher **não reproduz vídeos por si só** e não tem acesso aos catálogos privados dos serviços.
- Os pôsteres e logos dos canais são **imagens ilustrativas extraídas do conceito visual fornecido**, não uma programação em tempo real e não são feeds integrados. Os cartões tentam abrir o aplicativo compatível; se ele não existir, aparece uma mensagem.
- O fundo tem uma animação horizontal leve. Não há vídeo de fundo, para reduzir consumo de memória e CPU na TV HQ.
- A opção Favoritos nesta versão orienta a escolher um app da lista; não salva uma coleção persistente.

## Compilar com GitHub Actions

1. Extraia este ZIP sobre o repositório `vice-os-tv` local (preservando `.git` e arquivos não modificados). A pasta oculta `.github` deste ZIP precisa ser copiada também.
2. Rode `git add -A`, `git commit -m "CALIAN TV 1.0"`, `git push origin main`.
3. No GitHub abra **Actions > Gerar APK CALIAN TV** e selecione o run mais recente, ou clique em **Run workflow**.
4. Baixe **CALIAN-TV-1.0-APK**, descompacte e obtenha `app-debug.apk`.
5. Transfira o APK para a TV pelo **Send Files to TV** ou instale via ADB (`adb install -r app-debug.apk`).

## Cuidados para a instalação

- A versão debug criada no GitHub pode ter **assinatura diferente** do APK anterior: nesse caso o Android impedirá a atualização. Desinstale primeiro o VICE OS somente se souber que pode abrir o launcher original da TV HQ.
- Não desative nem exclua o launcher original da TV via ADB.
- TVs HQ podem não permitir escolher um launcher HOME personalizado; nesse caso, abra o CALIAN TV como um aplicativo normal.
- Para compilar localmente, use JDK 17, Android SDK 35 e Gradle 8.9 (ou Android Studio compatível).
