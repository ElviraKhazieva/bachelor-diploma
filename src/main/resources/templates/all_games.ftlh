<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/css/bootstrap.min.css" rel="stylesheet"
          integrity="sha384-GLhlTQ8iRABdZLl6O3oVMWSktQOp6b7In1Zl3/Jr59b6EGGoI1aFkw7cmDA6j6gD" crossorigin="anonymous">
    <title>Mои Игры</title>
</head>
<body>
<table id="account_games_table">
    <tr>
        <th>Название</th>
        <th>Начало</th>
        <th>Конец</th>
        <th>Статус</th>
        <th></th>
    </tr>
    <#list games as game>
        <tr>
            <td>${game.name!}</td>
            <td>${game.startDate!}</td>
            <td>${game.endDate!}</td>
            <td>${game.gameStatus.getName()!}</td>
            <td>
                <form action="/game/${game.id}">
                    <button>Перейти</button>
                </form>
            </td>
        </tr>
    </#list>
</table>
</body>
</html>