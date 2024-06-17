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
cd /opt/SuperApp || exit

# Компиляция проекта с помощью Maven
sudo mvn clean package

# Установка шрифтов
mkdir -p ~/.fonts
cp -r /opt/SuperApp/src/main/resources/fonts/* ~/.fonts

# Установка JavaFX
sudo mkdir -p /opt/javafx
cd /opt/javafx || exit
sudo curl -L -o openjfx.zip https://download2.gluonhq.com/openjfx/22.0.1/openjfx-22.0.1_linux-x64_bin-sdk.zip
sudo unzip -o openjfx.zip
sudo mv javafx-sdk-22.0.1/lib ./lib
sudo rm -rf javafx-sdk-22.0.1 openjfx.zip

# Копирование исполняемых файлов
sudo cp /opt/SuperApp/src/main/Process.sh /usr/local/bin/Process.sh
sudo cp /opt/SuperApp/src/main/System.sh /usr/local/bin/System.sh
sudo cp /opt/SuperApp/src/main/Terminal.sh /usr/local/bin/Terminal.sh

# Добавление приложения в меню
DESKTOP_ENTRY="[Desktop Entry]
Version=1.0
Name=SuperApp
Exec=java --module-path /opt/javafx/lib --add-modules javafx.controls,javafx.fxml -jar /opt/SuperApp/target/KR_SuperApp-1.0-SNAPSHOT-shaded.jar
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
if [ -d "/opt/SuperApp/scripts" ]; then
    cp -r /opt/SuperApp/scripts/* "$INSTALL_DIR/scripts/"
fi

# Добавляем команды в .bashrc, если они еще не добавлены
if ! grep -q "source $INSTALL_DIR/scripts/custom_bashrc" "$BASHRC"; then
    echo "source $INSTALL_DIR/scripts/custom_bashrc" >> "$BASHRC"
    echo "Пользовательские команды SuperApp добавлены в $BASHRC"
fi

# Применяем изменения
source "$BASHRC"

# Уведомление об успешной установке
echo "Установка завершена. Перезапустите терминал или выполните 'source ~/.bashrc' для применения изменений."
echo "Вы можете запустить приложение из меню или командой: java --module-path /opt/javafx/lib --add-modules javafx.controls,javafx.fxml -jar /opt/SuperApp/target/KR_SuperApp-1.0-SNAPSHOT-shaded.jar"
