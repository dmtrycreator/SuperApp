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
cp -r /opt/SuperApp/lib/fonts/* ~/.fonts

# Загрузка и установка JavaFX
JAVA_FX_VERSION="17.0.2"
curl -Lo openjfx.zip "https://gluonhq.com/download/javafx-${JAVA_FX_VERSION}-linux/"
sudo unzip openjfx.zip -d /opt/javafx

# Копирование исполняемых файлов и установка прав
sudo cp /opt/SuperApp/src/main/Process.sh /usr/local/bin/Process.sh
sudo cp /opt/SuperApp/src/main/System.sh /usr/local/bin/System.sh
sudo cp /opt/SuperApp/src/main/Terminal.sh /usr/local/bin/Terminal.sh
sudo chmod +x /usr/local/bin/Process.sh
sudo chmod +x /usr/local/bin/System.sh
sudo chmod +x /usr/local/bin/Terminal.sh

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
INSTALL_DIR="$HOME/.superapp"
BASHRC="$HOME/.bashrc"

# Создаем директории, если их нет
mkdir -p "$INSTALL_DIR/scripts"

# Копируем файлы в установочную директорию
cp -r /opt/SuperApp/scripts/* "$INSTALL_DIR/scripts/"

# Добавляем команды в .bashrc, если они еще не добавлены
if ! grep -q "source $INSTALL_DIR/scripts/custom_bashrc" "$BASHRC"; then
    echo "source $INSTALL_DIR/scripts/custom_bashrc" >> "$BASHRC"
    echo "Пользовательские команды SuperApp добавлены в $BASHRC"
fi

# Применяем изменения
source "$BASHRC"

# Уведомление об успешной установке
echo "Установка завершена. Перезапустите терминал или выполните 'source ~/.bashrc' для применения изменений."
echo "Вы можете запустить приложение из меню или командой: java --module-path /opt/javafx/lib --add-modules javafx.controls,javafx.fxml -jar /opt/SuperApp/target/KR_SuperApp-1.0-SNAPSHOT.jar"
