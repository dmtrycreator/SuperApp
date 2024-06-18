#!/bin/bash
# Установка необходимых пакетов
sudo apt update
sudo apt install -y openjdk-21-jdk git maven unzip

# Удаление старой версии приложения, если она существует
if [ -d "$HOME/SuperApp" ]; then
    rm -rf "$HOME/SuperApp"
fi

# Клонирование репозитория
git clone https://github.com/dmtrycreator/SuperApp.git "$HOME/SuperApp"

mkdir -p "$HOME/SuperApp/src/main"

# Переход в директорию проекта
cd "$HOME/SuperApp" || exit

# Компиляция проекта с помощью Maven
mvn clean package

# Установка шрифтов
mkdir -p ~/.fonts
if [ -d "$HOME/SuperApp/lib/fonts/Inter" ]; then
    cp -r "$HOME/SuperApp/lib/fonts/Inter/*" ~/.fonts
else
    echo "Каталог шрифтов не существует. Пропускаем копирование шрифтов."
fi

# Установка JavaFX
sudo mkdir -p /opt/javafx
cd /opt/javafx || exit
sudo curl -L -o openjfx.zip https://download2.gluonhq.com/openjfx/22.0.1/openjfx-22.0.1_linux-x64_bin-sdk.zip
sudo unzip -o openjfx.zip
sudo mv javafx-sdk-22.0.1/lib ./lib
sudo rm -rf javafx-sdk-22.0.1 openjfx.zip

# Копирование исполняемых файлов
for script in Process.sh System.sh Terminal.sh; do
    if [ -f "$HOME/SuperApp/src/main/$script" ]; then
        sudo cp "$HOME/SuperApp/src/main/$script" /usr/local/bin/$script
    fi
done

# Добавление приложения в меню
DESKTOP_ENTRY="[Desktop Entry]
Version=1.0
Name=SuperApp
Exec=java --module-path /opt/javafx/lib --add-modules javafx.controls,javafx.fxml -jar $HOME/SuperApp/target/KR_SuperApp-1.0-SNAPSHOT-shaded.jar
Icon=$HOME/SuperApp/src/main/resources/superapp/kr_superapp/icons/Icon_SuperApp.png
Type=Application
Categories=Utility;"

echo "$DESKTOP_ENTRY" > ~/.local/share/applications/SuperApp.desktop

# Установочные директории
INSTALL_DIR="$HOME/.superapp"
BASHRC="$HOME/.bashrc"

# Создаем директории, если их нет
mkdir -p "$INSTALL_DIR/scripts"

# Копируем файлы в установочную директорию
if [ -d "$HOME/SuperApp/scripts" ]; then
    cp -r "$HOME/SuperApp/scripts/*" "$INSTALL_DIR/scripts/"
fi

# Добавляем команды в .bashrc, если они еще не добавлены
if ! grep -q "source $INSTALL_DIR/scripts/custom_bashrc" "$BASHRC"; then
    echo "source $INSTALL_DIR/scripts/custom_bashrc" >> "$BASHRC"
    echo "Пользовательские команды SuperApp добавлены в $BASHRC"
fi

# Применяем изменения
source "$BASHRC"

# Уведомление об успешной установке
echo "Вы можете запустить приложение из меню или командой: java --module-path /opt/javafx/lib --add-modules javafx.controls,javafx.fxml -jar $HOME/SuperApp/target/KR_SuperApp-1.0-SNAPSHOT.jar"
