<h1>Search Engine Java</h1>

<p>Данное приложение является полноценным поисковым движком для индексации 
веб сайтов. Код написан на Java с использованием Spring Boot Framework.</p>

<h3 id="info">Что умеет данное веб приложение:</h3>
- Выполняет обход страниц сайтов, указанных в конфигурационном файле (см. <a href="#config" title="Конфигурация программы">"Конфигурация программы"</a>)
- Создает поисковые индексы всех найденных страниц
- Создает поисковый индекс для отдельной страницы
- Показывает общую статистику индексации
- Предоставляет возможность поиска по страницам проиндексированных сайтов
- После запуска доступен небольшой FRONT-END (на локальной машине - http://localhost:8080/)

<h2>Навигация</h2>
- <a href="#requiments" title="Системные требования">Системные требования</a>
- <a href="#api" title="API команды">API команды</a>
  - <a href="#api-indexing" title="Индексация">Индексация</a>
  - <a href="#api-indexing" title="Статистика">Статистика</a>
  - <a href="#api-indexing" title="Поиск">Поиск</a>
- <a href="#config" title="Конфигурация">Конфигурация</a>
  - <a href="#config-db" title="Подключение базы данных">Подключение базы данных</a>
  - <a href="#config-site-list" title="Добавление сайта в список для индексации">Добавление сайта в список для индексации</a>
  - <a href="#config-crawler" title="Настройка модуля обхода страниц">Настройка модуля обхода страниц</a>
  - <a href="#config-task-executor" title="Настройка выполнения многопоточных задач">Настройка выполнения многопоточных задач</a>
- <a href="#lucene" title="Подключение модуля Lucene Morphology">Подключение модуля Lucene Morphology</a>
- <a href="#developement-stuff" title="Особенности разработки приложения">Особенности разработки приложения</a>
- <a href="#credits" title="Благодарности">Благодарности</a>


<h2 id="api">Системные требования</h2>
- JDK v17+ *
- mySQL v8.20+ *
- Модуль Lucene Morphology (см. <a href="#lucene" title="Подключение Lucene Morphology">"Подключение Lucene Morphology"</a>)
<p><i>* Более ранние версии компонентов не тестировались</i></p>

<h2 id="api">API команды</h2>
<h3 id="api-indexing">ИНДЕКСАЦИЯ</h3>
<h4>Запуск полной индексации сайтов, указанных в конфигурационном файле</h4>
<code style="background-color: yellow; color: green">GET /api/startIndexing</code>
```json
/* ФОРМАТ ОТВЕТА */

{
  'result': true, //в случае успешного запуска
  'error': "error message" //сообщениие об ошибке в случае, если result: false
}
```
<h4>Остановка индексации всех сайтов:</h4>
<code style="background-color: yellow; color: green">GET /api/stopIndexing</code>
```json
/* ФОРМАТ ОТВЕТА */

{
  'result': true, //в случае успешного запуска
  'error': "error message" //сообщениие об ошибке в случае, если result: false
}
```
<h4>Запуск индексации отдельной страницы сайта из числа указанных в конфигурационном файле</h4>
<code style="background-color: yellow; color: green">POST /api/indexPage</code>
```json
/* ПАРАМЕТРЫ POST ЗАПРОСА */

{
  'url': "https://example.com/site-page/" //адрес страницы
}

/* ФОРМАТ ОТВЕТА */

{
  'result': true, //в случае успешного запуска
  'error': "error message" //сообщениие об ошибке в случае, если result: false
}
```
<h3 id="api-statistics">СТАТИСТИКА</h3>
<h4>Получения информации о созданных поисковых индексах и состоянии поискового движка</h4>
<code style="background-color: yellow; color: green">GET /api/statistics</code>
```json
/* ФОРМАТ ОТВЕТА В СЛУЧАЕ УСПЕХА */

{
  'result': true,
  'statistics': {
    "total": {
      "sites": 10,
      "pages": 436423,
      "lemmas": 5127891,
      "indexing": true
    },
    "detailed": [
      {
        "url": "http://www.example.com/",
        "name": "Имя сайта",
        "status": "INDEXED", // INDEXING, INDEXED, FAILED
        "statusTime": 1600160357,
        "error": "error message",
        "pages": 5764,
        "lemmas": 321115
      },
      ...
    ]
}
```
<h3 id="api-search">ПОИСК</h3>
<h4>Поиск по страницам проиндексированных сайтов</h4>
<code style="background-color: yellow; color: green">GET /api/search?{params}</code>
<h4>Параметры запроса:</h4>
- <b>query</b> — поисковый запрос;
- <b>site</b> — сайт, по которому осуществлять поиск. Если не указать, поиск будет выполнен по страницам всех проиндексированных сайтов;
- <b>offset</b> — сдвиг от 0 для постраничного вывода (необязательный параметр, значение по-умолчанию 0);
- <b>limit</b> — количество результатов, которое необходимо вывести (необязательный параметр, значение по-умолчанию 20).
```json
/* ФОРМАТ ОТВЕТА В СЛУЧАЕ УСПЕХА */

{
  'result': true,
  'statistics': {
    "total": {
      "sites": 10,
      "pages": 436423,
      "lemmas": 5127891,
      "indexing": true
    },
    "detailed": [
      {
        "url": "http://www.example.com/",
        "name": "Имя сайта",
        "status": "INDEXED", // INDEXING, INDEXED, FAILED
        "statusTime": 1600160357,
        "error": "error message",
        "pages": 5764,
        "lemmas": 321115
      },
      ...
    ]
}

/* ФОРМАТ ОТВЕТА В СЛУЧАЕ ОШИБКИ */

{
  'result': false,
  'error': "error message" //сообщениие об ошибке
}
```

<h2 id="config">Конфигурация</h2>
<p>Для настройки программы отредактируйте файл</p>
<code style="background-color: #7777ff; color: white">src/main/resources/application.yaml</code>
<h3 id="config-db">Подключение базы данных</h3>
<p><i>Введите имя базы данных mySQL, имя пользователя и пароль в соответствующие поля</i></p>

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/имя_базы_данных?useSSL=false&requireSSL=false&allowPublicKeyRetrieval=true
    username: имя_пользователя
    password: пароль
```
<h3 id="config-site-list">Добавление сайта в список для индексации</h3>
<p><i>Адрес сайта в формате <b>https://example.com</b>. 
<br>Если нужно включить в индекс только страницы из определенной "точки входа", 
можно указать url в следующем формате: <b>https://example.com/entry_point_path</b></i></p>

```yaml
indexing-settings:
  sites:
    - url: адрес_сайта
      name: название сайта
```
<h3 id="config-crawler">Настройка модуля обхода страниц</h3>
```yaml
crawler-settings:
  headers-rotation-interval-seconds: 300 # периодичность смены заголовков в секундах (параметр headers)
  connection-timeout-millis-min: 300 # таймаут подключения в миллисекундах, минимальное значение
  connection-timeout-millis-max: 2500 # таймаут подключения в миллисекундах, максимальное значение
  page-load-time-limit-millis: 15000 # лимит времени загрузки страницы в миллисекундах
  reconnect-attempts-max: 10 # количество попыток повторного соединения
  reconnect-attempt-timeout-millis: 5000 # таймаут между попытками повторного соединения
  headers: # добавьте собственные хедеры по образцу ниже
    - user-agent: "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/138.0.0.0 YaBrowser/25.8.0.0 Safari/537.36"
      referrer: "http://www.google.com"
      accept: "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,/;q=0.8"
      accept-language: "ru-RU,ru;q=0.9"
      accept-encoding: "gzip, deflate, br"
      upgrade-insecure-requests: "1"
```
<h3 id="config-task-executor">Настройка выполнения многопоточных задач</h3>

```yaml
task-executor:
  shutdown-await-termination-seconds: 15 # таймаут перед остановкой многопоточных процессов (например, процесса индексации), в секундах
```
<h2 id="lucene">Подключение модуля Lucene Morphology</h2>
<p>Для работы модуля морфологического анализа нужно вручную подключить
библиотеки Lucene Morphology</p>
<p>Для этого просто скачайте файлы по <a href="https://drive.google.com/file/d/1t5bfxfJKq0xdKS9ZbfIrA8gTojCD5neJ/view?usp=sharing">ССЫЛКЕ</a>.</p>
<p>Затем распакуйте содержимое архива (6 папок) в каталог:</p>
<code style="background-color: #7777ff; color: white">src/main/resources/lemmatizer</code>

<h2 id="developement-stuff">Особенности разработки приложения</h2>
<h3>Модуль лемматзации</h3>

<p>Программа использует технологию приведения слов к их 
морфологической базовой форме - лемме. Для работы данной функции требуется подключить библиотеку
<b>Lucene Morphology</b>.</p>

<p>Данное решение достаточно требовательно к ресурсам сервера - оперативной памяти и CPU.
Поэтому, во время индексации сайтов все данные модуля лемматизатора кешируются в оперативной памяти, 
а также сохраняется в базе данных. При перезапуске приложения кеш загружается
обратно в оперативную память.</p>

<h3>Поисковый запрос</h3>
<p>Коротко скажу лишь о том, что испытываю гордость и удовлетворение за
то, что вся поисковая выдача формируется лишь одним сложным нативным SQL запросом, что сильно экономит время формирования выдачи и ресурсы памяти. Реализацию можно посмотреть
в PageRepository пакета <code>src/repositories</code>.</p>
<p>Работа модуля поиска тесно связана с синтаксисом mySQL, однако, прии желани,
можно использовать более гибкую и функциональную PostgreSQL</p>

<h3>Модуль формирования снипетта найденной страницы</h3>

<p>Немало времени заняла работа над формированием короткой выдержки из
текста страницы с подсветкой поисковых слов. Алгоритм основан на том, что сначала в основную
морфологическую форму переводятся слова поисковой фразы и текст проиндексированной страницы. Далее осуществляется поиск
 вхождений каждого слова в тексте.</p>
<p>Из массивов индексов найденных слов формируются своеобразные "области" контента (Content Range). 
Большие области разбиваются на более мелкие. Вокруг каждой области формируется контекст из определенного количества слов.</p>
<p>Самые релевантные запросу сниппеты ставятся вначале поисковой выдачи.</p>

<h2 id="credits">Благодарности</h2>
<p>Я бы хотел поблагодарить за этот прекрасный опыт команду <b style="color: white; background-color: blue; padding: 5px">Skillbox</b>, 
всех моих кураторов, которые проверяли практические работы и давали наставления.</p>
<p>Огромная благодарность за возможность реализовать свой первый достаточно объемный проект,
за опыт и положительные эмоции, полученные в процессе обучения!</p>
