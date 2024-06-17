#!/bin/bash

# Установка необходимых пакетов
sudo apt update
sudo apt install -y openjdk-21-jdk git maven unzip

# Путь к вашему проекту
PROJECT_DIR="$HOME/SuperApp"

# Удаление старой версии приложения, если она существует
if [ -d "$PROJECT_DIR" ]; then
    rm -rf "$PROJECT_DIR"
fi

# Клонирование репозитория
git clone https://github.com/dmtrycreator/SuperApp.git "$PROJECT_DIR"

# Переход в директорию проекта
cd "$PROJECT_DIR"

# Компиляция проекта с помощью Maven
mvn clean package

# Установка шрифтов (если есть)
if [ -d "$PROJECT_DIR/src/main/resources/fonts" ]; then
    mkdir -p ~/.fonts
    cp -r "$PROJECT_DIR/src/main/resources/fonts/*" ~/.fonts
else
    echo "Каталог шрифтов не существует. Пропускаем копирование шрифтов."
fi

# Установка JavaFX
JAVA_FX_DIR="/opt/javafx"
sudo mkdir -p "$JAVA_FX_DIR"
cd "$JAVA_FX_DIR"
sudo curl -L -o openjfx.zip https://download2.gluonhq.com/openjfx/22.0.1/openjfx-22.0.1_linux-x64_bin-sdk.zip
sudo unzip -o openjfx.zip
sudo mv javafx-sdk-22.0.1/lib ./lib
sudo rm -rf javafx-sdk-22.0.1 openjfx.zip

# Копирование исполняемых файлов (если есть)
if [ -f "$PROJECT_DIR/src/main/Process.sh" ]; then
    sudo cp "$PROJECT_DIR/src/main/Process.sh" /usr/local/bin/Process.sh
fi

if [ -f "$PROJECT_DIR/src/main/System.sh" ]; then
    sudo cp "$PROJECT_DIR/src/main/System.sh" /usr/local/bin/System.sh
fi

if [ -f "$PROJECT_DIR/src/main/Terminal.sh" ]; then
    sudo cp "$PROJECT_DIR/src/main/Terminal.sh" /usr/local/bin/Terminal.sh
fi

# Добавление приложения в меню
DESKTOP_ENTRY="[Desktop Entry]
Version=1.0
Name=SuperApp
Exec=java --module-path /opt/javafx/lib --add-modules javafx.controls,javafx.fxml -jar $PROJECT_DIR/target/KR_SuperApp-1.0-SNAPSHOT-shaded.jar
Icon=$PROJECT_DIR/src/main/resources/superapp/kr_superapp/icons/Icon_SuperApp.png
Type=Application
Categories=Utility;"

echo "$DESKTOP_ENTRY" > ~/.local/share/applications/SuperApp.desktop

# Установочные директории
INSTALL_DIR="$HOME/.superapp"
BASHRC="$HOME/.bashrc"

# Создаем директории, если их нет
mkdir -p "$INSTALL_DIR/scripts"

# Копируем файлы в установочную директорию (если есть)
if [ -d "$PROJECT_DIR/scripts" ]; then
    cp -r "$PROJECT_DIR/scripts/*" "$INSTALL_DIR/scripts/"
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
echo "Вы можете запустить приложение из меню или командой: java --module-path /opt/javafx/lib --add-modules javafx.controls,javafx.fxml -jar $PROJECT_DIR/target/KR_SuperApp-1.0-SNAPSHOT-shaded.jar"
