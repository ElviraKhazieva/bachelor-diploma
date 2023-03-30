<!DOCTYPE html>
<head xmlns="http://www.w3.org/1999/html">
    <title>Редактирование профиля</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/css/bootstrap.min.css" rel="stylesheet"
          integrity="sha384-GLhlTQ8iRABdZLl6O3oVMWSktQOp6b7In1Zl3/Jr59b6EGGoI1aFkw7cmDA6j6gD" crossorigin="anonymous">
</head>
<body>
<form action="/profile" method="post">
    <label for="fullName">Полное имя</label>
    <input type="text" name="fullName" id="fullName" placeholder="Введите новое имя">
    <input type="submit" value="Сохранить">
</form>
</body>
</html>