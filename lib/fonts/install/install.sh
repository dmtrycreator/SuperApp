#!/bin/bash

# Установка необходимых пакетов
sudo apt update
sudo apt install -y openjdk-21-jdk git maven

# Удаление старой версии приложения, если она существует
if [ -d "/opt/SuperApp" ]; then
    sudo rm -rf /opt/SuperApp
fi

# Клонирование репозитория
git clone https://github.com/dmtrycreatorE/SuperApp.git /opt/SuperApp

# Переход в директорию проекта
cd /opt/SuperApp

# Компиляция проекта с помощью Maven
mvn clean package

# Установка шрифтов
mkdir -p ~/.fonts
cp -r lib/fonts/* ~/.fonts

# Добавление приложения в меню
DESKTOP_ENTRY="[Desktop Entry]
Version=1.0
Name=SuperApp
Exec=java -jar /opt/SuperApp/target/KR_SuperApp-1.0-SNAPSHOT.jar
Icon=/opt/SuperApp/icon.png
Type=Application
Categories=Utility;"

echo "$DESKTOP_ENTRY" > ~/.local/share/applications/SuperApp.desktop

# Уведомление об успешной установке
echo "Установка завершена. Вы можете запустить приложение из меню или командой: java -jar /opt/SuperApp/target/KR_SuperApp-1.0-SNAPSHOT.jar"
