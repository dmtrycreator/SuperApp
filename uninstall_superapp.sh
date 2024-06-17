#!/bin/bash

# Директории и файлы, которые нужно удалить
INSTALL_DIR="$HOME/SuperApp"
DESKTOP_ENTRY="$HOME/.local/share/applications/SuperApp.desktop"

# Удаление установочной директории
if [ -d "$INSTALL_DIR" ]; then
    rm -rf "$INSTALL_DIR"
    echo "Установочная директория удалена: $INSTALL_DIR"
else
    echo "Установочная директория не найдена: $INSTALL_DIR"
fi

# Удаление значка из меню приложений
if [ -f "$DESKTOP_ENTRY" ]; then
    rm "$DESKTOP_ENTRY"
    echo "Значок приложения удален из меню: $DESKTOP_ENTRY"
else
    echo "Значок приложения в меню не найден: $DESKTOP_ENTRY"
fi

# Удаление jar-файла приложения
JAR_PATH="/opt/SuperApp/target/KR_SuperApp-1.0-SNAPSHOT.jar"
if [ -f "$JAR_PATH" ]; then
    sudo rm "$JAR_PATH"
    echo "JAR-файл приложения удален: $JAR_PATH"
else
    echo "JAR-файл приложения не найден: $JAR_PATH"
fi

# Удаление папки с исходным кодом приложения
SOURCE_DIR="/opt/SuperApp"
if [ -d "$SOURCE_DIR" ]; then
    sudo rm -rf "$SOURCE_DIR"
    echo "Папка с исходным кодом удалена: $SOURCE_DIR"
else
    echo "Папка с исходным кодом не найдена: $SOURCE_DIR"
fi

echo "Удаление приложения завершено."
