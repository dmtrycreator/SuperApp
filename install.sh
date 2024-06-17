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

# Перемещение необходимых директорий
mkdir -p /opt/SuperApp/src/main
mv /opt/SuperApp/home /opt/SuperApp/src/main/home
mv /opt/SuperApp/trash /opt/SuperApp/src/main/trash

# Переход в директорию проекта
cd /opt/SuperApp || exit

# Компиляция проекта с помощью Maven
sudo mvn clean package

# Установка шрифтов
mkdir -p ~/.fonts
if [ -d "/opt/SuperApp/src/main/resources/fonts" ]; then
    cp -r /opt/SuperApp/src/main/resources/fonts/* ~/.fonts
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
    if [ -f "/opt/SuperApp/src/main/$script" ]; then
        sudo cp "/opt/SuperApp/src/main/$script" /usr/local/bin/$script
    fi
done

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
