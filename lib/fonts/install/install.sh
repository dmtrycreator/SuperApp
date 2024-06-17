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

# Установочные директории
INSTALL_DIR="$HOME/.superapp"
BASHRC="$HOME/.bashrc"

# Создаем директории, если их нет
mkdir -p "$INSTALL_DIR/scripts"

# Копируем файлы в установочную директорию
cp -r scripts/* "$INSTALL_DIR/scripts/"

# Добавляем команды в .bashrc, если они еще не добавлены
if ! grep -q "source $INSTALL_DIR/scripts/custom_bashrc" "$BASHRC"; then
    echo "source $INSTALL_DIR/scripts/custom_bashrc" >> "$BASHRC"
    echo "Пользовательские команды SuperApp добавлены в $BASHRC"
fi

# Применяем изменения
source "$BASHRC"

# Уведомление об успешной установке
echo "Установка завершена. Перезапустите терминал или выполните 'source ~/.bashrc' для применения изменений."
echo "Вы можете запустить приложение из меню или командой: java -jar /opt/SuperApp/target/KR_SuperApp-1.0-SNAPSHOT.jar"
