<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/css/bootstrap.min.css" rel="stylesheet"
          integrity="sha384-GLhlTQ8iRABdZLl6O3oVMWSktQOp6b7In1Zl3/Jr59b6EGGoI1aFkw7cmDA6j6gD" crossorigin="anonymous">
    <title>Управление игрой</title>
</head>
<body>
<button id="schedulerButton" onclick="toggleScheduler()">Запустить торговые сессии</button>

<table id="manufacturers_table">
    <tr>
        <th>ID</th>
        <th>Баланс</th>
    </tr>
    <#list manufacturers as manufacturer>
        <tr>
            <td>${manufacturer.id!}</td>
            <td>${manufacturer.balance!}</td>
        </tr>
    </#list>
<script>
    let startedTradingSessions = ${startedTradingSessions?c};
    let schedulerPeriod = ${game.timeUnit};

    if (startedTradingSessions) {
        document.getElementById("schedulerButton").innerText = "Остановить торговые сессии";
    } else {
        document.getElementById("schedulerButton").innerText = "Запустить торговые сессии";
    }

    function toggleScheduler() {
        if (!startedTradingSessions) {
            startScheduler();
        } else {
            stopScheduler();
        }
    }

    function startScheduler() {
        fetch("/game/${game.id}/trading-sessions/start/"+ schedulerPeriod)
    .then(response => response.text())
            .then(result => {
                console.log(result);
                startedTradingSessions = true;
                document.getElementById("schedulerButton").innerText = "Остановить торговые сессии";
            })
            .catch(error => console.error("Error:", error));
    }

    function stopScheduler() {
        fetch("/game/${game.id}/trading-sessions/stop")
            .then(response => response.text())
            .then(result => {
                console.log(result);
                startedTradingSessions = false;
                document.getElementById("schedulerButton").innerText = "Запустить торговые сессии";
            })
            .catch(error => console.error("Error:", error));
    }
</script>
</body>
</html>