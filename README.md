# SuperApp

### Описание проекта

SuperApp — это файловый менеджер, разработанный в рамках курсовой работы. Он предоставляет пользователям удобный интерфейс для навигации по файловой системе, управления файлами и папками, а также взаимодействия с системными процессами. Основные возможности включают:

- Просмотр и управление файлами и папками
- Встроенный терминал с набором команд
- Информация о системных процессах
- Удобный графический интерфейс с контекстными меню и горячими клавишами

### Установка

#### Предварительные требования

Перед установкой убедитесь, что на вашем компьютере установлены следующие компоненты:

- Java Development Kit (JDK) версии 21
- Apache Maven
- Git
- Утилита unzip

Вы можете установить их с помощью следующих команд:

```bash
sudo apt update
sudo apt install -y openjdk-21-jdk git maven unzip
```

#### Шаги установки

1. **Клонирование репозитория и запуск скрипта установки**

   Выполните следующие команды в терминале:

   ```bash
   cd ~
   git clone https://github.com/dmtrycreator/SuperApp.git
   chmod +x SuperApp/install.sh
   ./SuperApp/install.sh
   ```

2. **Удаление приложения**

   Для удаления выполните:

   ```bash
   cd ~
   chmod +x SuperApp/uninstall_superapp.sh
   ./SuperApp/uninstall_superapp.sh
   ```
   
3. **Установка необходимых компонентов**

   Скрипт `install.sh` выполнит следующие действия:
   
   - Обновит списки пакетов и установит необходимые зависимости
   - Удалит старую версию приложения (если она существует)
   - Клонирует репозиторий SuperApp в директорию `/opt/SuperApp`
   - Соберет проект с помощью Maven
   - Установит шрифты и JavaFX
   - Добавит исполняемые файлы в системные директории
   - Добавит приложение в меню с указанием иконки и командой запуска

4. **Запуск приложения**

   После успешной установки вы можете запустить приложение из меню или с помощью команды:

   ```bash
   java --module-path /opt/javafx/lib --add-modules javafx.controls,javafx.fxml -jar /opt/SuperApp/target/KR_SuperApp-1.0-SNAPSHOT.jar
   ```

### Использование

После запуска приложения откроется основное окно с древовидной структурой файловой системы слева и сеткой файлов справа. Основные функции включают:

- **Навигация по директориям**: Используйте дерево файлов для перехода между папками.
- **Управление файлами**: Копирование, перемещение, удаление и переименование файлов с помощью контекстного меню (ПКМ).
- **Встроенный терминал**: Поддерживает базовые сетевые команды и команды для работы с файловой системой.
- **Информация о системе**: Вкладка "О системе" показывает информацию о процессоре, памяти и текущих процессах.
- **Корзина**: Удаленные файлы перемещаются в корзину, откуда их можно удалить навсегда.

### Поддержка

Если у вас возникли вопросы или проблемы с использованием SuperApp, пожалуйста, свяжитесь с разработчиком по следующему адресу: [d.zadisentsev@yandex.ru].

### Лицензия

Этот проект нелицензирован.
