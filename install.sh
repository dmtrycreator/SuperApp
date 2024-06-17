#!/bin/bash

# Установка необходимых пакетов
sudo apt update
sudo apt install -y openjdk-21-jdk git maven unzip

# Удаление старой версии приложения, если она существует
if [ -d "/opt/SuperApp" ]; then
    sudo rm -rf /opt/SuperApp
fi

# Клонирование репозитория
sudo git clone https://github.com/dmtrycreator/SuperApp.git /opt/SuperApp

# Переход в директорию проекта
cd /opt/SuperApp

# Компиляция проекта с помощью Maven
sudo mvn clean package

# Установка шрифтов
mkdir -p ~/.fonts
cp -r /opt/SuperApp/src/main/fonts/* ~/.fonts

# Установка JavaFX
sudo mkdir -p /opt/javafx
cd /opt/javafx
sudo curl -L -o openjfx.zip https://download2.gluonhq.com/openjfx/22.0.1/openjfx-22.0.1_linux-x64_bin-sdk.zip
sudo unzip openjfx.zip
sudo mv javafx-sdk-22.0.1/lib ./
sudo rm -rf javafx-sdk-22.0.1 openjfx.zip

# Копирование исполняемых файлов
sudo cp /opt/SuperApp/src/main/Process.sh /usr/local/bin/Process.sh
sudo cp /opt/SuperApp/src/main/System.sh /usr/local/bin/System.sh
sudo cp /opt/SuperApp/src/main/Terminal.sh /usr/local/bin/Terminal.sh

# Добавление приложения в меню
DESKTOP_ENTRY="[Desktop Entry]
Version=1.0
Name=SuperApp
Exec=java --module-path /opt/javafx/lib --add-modules javafx.controls,javafx.fxml -jar /opt/SuperApp/target/KR_SuperApp-1.0-SNAPSHOT.jar
Icon=/opt/SuperApp/src/main/resources/superapp/kr_superapp/icons/Icon_SuperApp.png
Type=Application
Categories=Utility;"

echo "$DESKTOP_ENTRY" > ~/.local/share/applications/SuperApp.desktop

# Установочные директории
INSTALL_DIR="$HOME/SuperApp"

# Создаем установочные директории
mkdir -p "$INSTALL_DIR/home"
mkdir -p "$INSTALL_DIR/trash"
mkdir -p "$INSTALL_DIR/fonts"
mkdir -p "$INSTALL_DIR/javafx/lib"

# Копируем файлы в установочную директорию
cp -r /opt/SuperApp/src/main/resources/fonts/* "$INSTALL_DIR/fonts/"

# Добавляем приложение в меню приложений
DESKTOP_ENTRY="[Desktop Entry]
Version=1.0
Name=SuperApp
Exec=java --module-path /opt/javafx/lib --add-modules javafx.controls,javafx.fxml -jar $INSTALL_DIR/KR_SuperApp-1.0-SNAPSHOT.jar
Icon=$INSTALL_DIR/src/main/resources/superapp/kr_superapp/icons/Icon_SuperApp.png
Type=Application
Categories=Utility;"

echo "$DESKTOP_ENTRY" > ~/.local/share/applications/SuperApp.desktop

# Создание файла флага установки
touch "$INSTALL_DIR/.installed"

# Уведомление об успешной установке
echo "Установка завершена. Вы можете запустить приложение из меню или командой: java --module-path /opt/javafx/lib --add-modules javafx.controls,javafx.fxml -jar $INSTALL_DIR/KR_SuperApp-1.0-SNAPSHOT.jar"
