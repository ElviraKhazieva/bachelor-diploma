<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/css/bootstrap.min.css" rel="stylesheet"
          integrity="sha384-GLhlTQ8iRABdZLl6O3oVMWSktQOp6b7In1Zl3/Jr59b6EGGoI1aFkw7cmDA6j6gD" crossorigin="anonymous">
    <title>Профиль Админа</title>
</head>
<body>
<h1>Мой профиль</h1>
<p>Полное имя: ${account.fullName!}</p>
<p>Почта: ${account.email!}</p>
<a class="btn btn-outline-primary" href="/profile/edit" role="button">Редактировать профиль</a>
<a class="btn btn-outline-primary" href="/game" role="button">Новая игра</a>
<a class="btn btn-outline-primary" href="/games" role="button">Все игры</a>
<a class="btn btn-outline-primary" href="/logout" role="button">Выход</a>

<#--<form action="/game">-->
<#--    <input type="submit" value="Новая игра"/>-->
<#--</form>-->
<#--<form action="/profile/edit">-->
<#--    <input type="submit" value="Редактировать профиль"/>-->
<#--</form>-->
<#--<form action="/all-games">-->
<#--    <input type="submit" value="Все игры"/>-->
<#--</form>-->

<#--<h2><a href="/logout">Выход</a></h2>-->
</body>
</html>