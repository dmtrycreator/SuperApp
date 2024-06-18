#!/bin/bash

# Убедитесь, что скрипт выполняется из домашнего каталога
if [ "$PWD" != "$HOME" ]; then
    echo "Ошибка: пожалуйста, выполните скрипт из вашего домашнего каталога."
    exit 1
fi

# Установка необходимых пакетов
sudo apt update
sudo apt install -y openjdk-21-jdk git maven unzip

# Удаление старой версии приложения, если она существует
if [ -d "$HOME/SuperApp" ]; then
    rm -rf "$HOME/SuperApp"
fi

# Клонирование репозитория
if ! git clone https://github.com/dmtrycreator/SuperApp.git "$HOME/SuperApp"; then
    echo "Ошибка: не удалось клонировать репозиторий в $HOME/SuperApp"
    exit 1
fi

# Проверка успешного клонирования
if [ ! -d "$HOME/SuperApp" ]; then
    echo "Ошибка: не удалось клонировать репозиторий в $HOME/SuperApp"
    exit 1
fi

# Переход в директорию проекта
cd "$HOME/SuperApp" || { echo "Не удалось перейти в директорию $HOME/SuperApp"; exit 1; }

# Компиляция проекта с помощью Maven
mvn clean package

# Установка шрифтов
mkdir -p ~/.fonts
if [ -d "$HOME/SuperApp/src/main/fonts/Inter" ]; then
    cp -r "$HOME/SuperApp/src/main/fonts/Inter/*" ~/.fonts
else
    echo "Каталог шрифтов не существует. Пропускаем копирование шрифтов."
fi

# Установка JavaFX
sudo mkdir -p /opt/javafx
cd /opt/javafx || { echo "Не удалось перейти в директорию /opt/javafx"; exit 1; }
sudo curl -L -o openjfx.zip https://download2.gluonhq.com/openjfx/22.0.1/openjfx-22.0.1_linux-x64_bin-sdk.zip
sudo unzip -o openjfx.zip
sudo cp -r javafx-sdk-22.0.1/lib /opt/javafx
sudo rm -rf javafx-sdk-22.0.1 openjfx.zip

# Копирование исполняемых файлов
for script in Process.sh System.sh Terminal.sh; do
    if [ -f "$HOME/SuperApp/src/main/$script" ]; then
        sudo cp "$HOME/SuperApp/src/main/$script" /usr/local/bin/$script
    fi
done

# Создание отсутствующих директорий
mkdir -p "$HOME/SuperApp/src/main/home"
mkdir -p "$HOME/SuperApp/src/main/trash"

# Добавление приложения в меню
DESKTOP_ENTRY="[Desktop Entry]
Version=1.0
Name=SuperApp
Exec=java --module-path /opt/javafx/lib --add-modules javafx.controls,javafx.fxml -jar $HOME/SuperApp/target/KR_SuperApp-1.0-SNAPSHOT-shaded.jar
Icon=$HOME/SuperApp/src/main/resources/superapp/kr_superapp/icons/Icon_SuperApp.png
Type=Application
Categories=Utility;"

echo "$DESKTOP_ENTRY" > ~/.local/share/applications/SuperApp.desktop

# Уведомление об успешной установке
echo "Вы можете запустить приложение из меню или командой: java --module-path /opt/javafx/lib --add-modules javafx.controls,javafx.fxml -jar $HOME/SuperApp/target/KR_SuperApp-1.0-SNAPSHOT-shaded.jar"
