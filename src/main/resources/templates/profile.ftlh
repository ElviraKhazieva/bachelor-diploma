<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/css/bootstrap.min.css" rel="stylesheet"
          integrity="sha384-GLhlTQ8iRABdZLl6O3oVMWSktQOp6b7In1Zl3/Jr59b6EGGoI1aFkw7cmDA6j6gD" crossorigin="anonymous">
    <title>Профиль Производителя</title>
</head>
<body>
<h1>Мой профиль</h1>
<p>Полное имя: ${account.fullName!}</p>
<p>Почта: ${account.email!}</p>
<a class="btn btn-outline-primary" href="/profile/edit" role="button">Редактировать профиль</a>
<a class="btn btn-outline-primary" href="/${account.id}/games" role="button">Мои игры</a>
<a class="btn btn-outline-primary" href="/logout" role="button">Выход</a>

</body>
</html>