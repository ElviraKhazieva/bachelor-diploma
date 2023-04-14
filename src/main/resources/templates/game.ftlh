<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Игра</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/css/bootstrap.min.css" rel="stylesheet">
    <style>
        .footer {
            position: absolute;
            bottom: 0;
            width: 100%;
            height: 60px; /* Высота футера */
        }

        html {
            position: relative;
            min-height: 100%;
        }

        #tab1-image {
            background-image: url('https://courseburg.ru/obuchenie_personala_v_organizacii/images/tild3238-6166-4132-b463-393434663337__clip-calculating.png');
            background-size: cover;
            background-position: center;
            height: 450px;
            width: 500px;
            display: none;
        }

        #tab2-image {
            background-image: url('https://courseburg.ru/obuchenie_personala_v_organizacii/images/tild6361-3637-4137-a166-393435636634__clip-covid-19-spread.png');
            background-size: cover;
            background-position: center;
            height: 300px;
            width: 300px;
            display: none;
        }

    </style>
</head>
<body>
<nav class="navbar navbar-expand-lg navbar-dark bg-primary">
    <div class="container-fluid">
        <a class="navbar-brand" href="/">Economic Concurrent Game</a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav"
                aria-controls="navbarNav" aria-expanded="false" aria-label="Toggle navigation">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNav">
            <ul class="navbar-nav ms-auto">
                <li class="nav-item">
                    <a class="nav-link active" aria-current="page" href="/profile">Профиль</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" href="/logout">Выйти</a>
                </li>
            </ul>
        </div>
    </div>
</nav>

<div class="container-fluid mt-5" style="margin-bottom: 150px">
    <ul class="nav nav-tabs" style="margin-left:50px; margin-right:50px" id="myTab" role="tablist">
        <li class="nav-item" role="presentation">
            <a class="nav-link active" id="tab1-tab" data-bs-toggle="tab" href="#tab1" role="tab" aria-controls="tab1"
               aria-selected="true">Главная</a>
        </li>
        <li class="nav-item" role="presentation">
            <a class="nav-link" id="tab2-tab" data-bs-toggle="tab" href="#tab2" role="tab" aria-controls="tab2"
               aria-selected="false">Моя Статистика</a>
        </li>
        <li class="nav-item" role="presentation">
            <a class="nav-link" id="tab3-tab" data-bs-toggle="tab" href="#tab3" role="tab" aria-controls="tab3"
               aria-selected="false">Конкуренты</a>
        </li>
    </ul>
    <div class="tab-content">
        <div class="tab-pane fade show active" id="tab1" role="tabpanel"
             aria-labelledby="tab1-tab">
            <div class="row justify-content-center" style="margin-left: 50px; margin-right: 50px">
                <div class="col-4 d-flex justify-content-center">
                    <div id="tab1-image" class="align-self-center image-container"></div>
                </div>
                <div class="col-4" style="margin-top:60px">
                    <h2>Финансовое состояние</h2>
                    <p id="day">День: -</p>
                    <p id="balance">Баланс: -</p>
                    <p>Сумма инвестиционного кредита: ${manufacturer.investmentCreditAmount!}</p>
                    <p id="investmentCreditDebt">Долг по инвестиционному кредиту: -</p>
                    <p id="businessCreditDebt">Долг по оборотным кредитам: -</p>
                    <p id="todayRevenue">Выручка за сегодня: -</p>
                    <button id="refreshTab1" style="margin-top: 10px" class="btn btn-primary">Обновить</button>
                </div>

                <div class="col-4" style="margin-top:60px">
                    <h2>Параметры игры</h2>
                    <p>Модельное время(Длительность 1 суток) - ${game.timeUnit} мин</p>
                    <p>Налог с продаж - ${game.salesTax}%</p>
                    <p>Процентная ставка инвестиционного кредита - ${game.interestRateInvestmentCredit}%</p>
                    <p>Срок инвестиционного кредита - ${game.interestRateInvestmentCredit} мес</p>
                </div>
                <#--                <div class="container-fluid mt-5">-->
                <#--                    <div class="row justify-content-center" style="margin-left: 50px; margin-right: 50px">-->
                <div class="col-md-12" style="margin-top: 60px">
                    <h2>Параметры производства:</h2>
                    <div class="table-responsive" style="height: 300px;">
                        <table id="dataTable" class="table table-striped">
                            <thead>
                            <tr>
                                <th>День начала выпуска продукции</th>
                                <th>Число дней выпуска продукции</th>
                                <th>Количество продукции</th>
                                <th>Цена(шт)</th>
                                <th>Себестоимость(шт)</th>
                                <th>Ассортимент</th>
                                <th>Индекс качества</th>
                                <th>Мощность производства(ед/сутки)</th>
                                <th>День начала рекламы</th>
                                <th>День завершения рекламы</th>
                                <th>Стоимость рекламы</th>
                                <th>Индекс интенсивности рекламы</th>
                                <th>Сумма оборотного кредита</th>
                                <th>Процентная ставка оборотного кредита</th>
                            </tr>
                            </thead>
                            <tbody>
                            <#list productionParameters as parameters>
                                <tr>
                                    <td>${parameters.startDate + 1}</td>
                                    <td>${parameters.timeToMarket!}</td>
                                    <td>${parameters.productCount!}</td>
                                    <td>${parameters.price!}</td>
                                    <td>${parameters.costPrice!}</td>
                                    <td>${parameters.assortment!}</td>
                                    <td>${parameters.qualityIndex!}</td>
                                    <td>${parameters.productionCapacityPerDay!}</td>
                                    <td>${parameters.advertisingStartDate!}</td>
                                    <td>${parameters.advertisingEndDate!}</td>
                                    <td>${parameters.advertisingCost!}</td>
                                    <td>${parameters.advertisingIntensityIndex!}</td>
                                    <td>${parameters.businessCreditAmount!}</td>
                                    <td>${parameters.interestRateBusinessCredit!}</td>
                                </tr>
                            </#list>
                            </tbody>
                        </table>
                    </div>
                    <h4 id="timeToMarketEndDay">Последний день выпуска продукции текущего производственного
                        цикла: -</h4>
                </div>
                <div class="input-group">
                    <button class="btn btn-primary" type="button" id="enterParametersButton"
                            onclick="window.location.href='/game/${game.id}/production-parameters';">Определить
                        новые параметры производства
                    </button>
                </div>
            </div>
        </div>
        <div class="tab-pane fade" id="tab2" role="tabpanel" aria-labelledby="tab2-tab">
            <#--            <div id="tab2-image" class="image-container"></div>-->
            <div class="container d-flex justify-content-center">
                <div class="row">
                    <div class="col-12">
                        <div style="margin-top: 15px">
                            <canvas width="800" height="450" id="chart1"></canvas>
                        </div>
                    </div>
                    <div class="col-12">
                        <div style="margin-top: 10px">
                            <canvas width="800" height="450" id="chart2"></canvas>
                        </div>
                    </div>

                    <div>
                        <button id="refreshTab2" style="margin-top: 10px" class="btn btn-primary">Обновить</button>
                    </div>
                </div>
            </div>
        </div>

        <div class="tab-pane fade" id="tab3" role="tabpanel" aria-labelledby="tab3-tab">
            <div class="container d-flex justify-content-center">
                <div class="row">
                    <#list competitors as competitor>
                        <div style="margin-top: 15px">
                            <h3>${competitor.account.fullName}</h3>
                            <h4>Цена товара: </h4>
                            <canvas id="chartt${competitor.id}" width="800" height="450"></canvas>
                        </div>
                    </#list>
                    <div class="col-12 mt-3">
                        <button class="btn btn-primary" onclick="updateCharts()">Обновить графики</button>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>


<footer class="footer d-flex flex-column flex-md-row text-center text-md-start justify-content-between py-4 px-4 px-xl-5 bg-primary">
    <div class="text-white mb-3">
        Copyright © 2023. All rights reserved.
    </div>
</footer>

<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/@popperjs/core@2.11.6/dist/umd/popper.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/js/bootstrap.min.js"></script>
<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
<script>
    const tabContent = document.getElementById('tab-content');
    const tabLinks = document.querySelectorAll('.nav-link');

    tabLinks.forEach(link => {
        link.addEventListener('shown.bs.tab', () => {
            // Получаем id отображаемой вкладки
            const tabId = link.getAttribute('href');

            // Очищаем содержимое предыдущей вкладки, если её id не соответствует текущей вкладке
            const previousTabContent = tabContent.querySelector('.tab-pane.show');
            if (previousTabContent && previousTabContent.id !== tabId.slice(1)) {
                tabContent.removeChild(previousTabContent);
            }
        });
    });
    let chartInstance;

    function fetchBalanceAndDebt() {
        $.ajax({
            url: 'http://localhost:80/game/' + ${game.id} + '/financial-status/' + ${account.id},
            type: 'GET',
            dataType: 'json',
            success: function (data) {
                document.getElementById('day').innerText = 'День: ' + data.day;
                document.getElementById('balance').innerText = 'Баланс: ' + data.balance;
                document.getElementById('investmentCreditDebt').innerText = 'Долг по инвестиционному кредиту: ' + data.investmentCreditDebt;
                document.getElementById('businessCreditDebt').innerText = 'Долг по оборотному кредиту: ' + data.businessCreditDebt;
                document.getElementById('todayRevenue').innerText = 'Выручка за сегодня: ' + data.todayRevenue;
                document.getElementById('timeToMarketEndDay').innerText = 'Последний день выпуска продукции текущего производственного цикла: ' + data.timeToMarketEndDay;
                const enterParametersButton = document.getElementById('enterParametersButton');
                enterParametersButton.disabled = data.activeManufacturingCycle;
            },
            error: function (error) {
                console.log('Ошибка: ', error);
            },
        });
    }


    // Получаем данные по HTTP-запросу
    let data = {};

    function fetchData() {
        fetch('http://localhost:80/game/revenue-data/' + ${manufacturer.id})
            .then(response => response.json())
            .then(newData => {
                // создаем массив данных для графика
                let chartData = [];
                // определяем максимальный ключ в мапе
                let maxKey = Math.max.apply(Math, Object.keys(newData));
                const labels = Array.from({length: maxKey}, (_, i) => i + 1);
                // проходим циклом по всем возможным ключам
                for (let i = 1; i <= maxKey; i++) {
                    // если значение для ключа существует в мапе, добавляем его в массив данных
                    if (i in newData) {
                        chartData.push(newData[i]);
                    } else { // иначе добавляем ноль
                        chartData.push(0);
                    }
                }
                // Обновляем график
                chart1.data.labels = labels;
                chart1.data.datasets[0].data = chartData;
                chart1.update();
            });

        fetch('http://localhost:80/game/' + ${game.id} + '/advertising-intensity/' + ${account.id})
            .then(response => response.json())
            .then(data => {
                // Находим максимальный номер дня в списке объектов
                const maxDay = Math.max(...data.map(item => item.endDate));

// Создаем массив с метками по оси x от 1 до максимального номера дня
                const labels = Array.from({length: maxDay}, (_, i) => i + 1);

// Создаем массив данных для графика
                const chartData = labels.map(day => {
                    // Находим объект в списке, для которого дни включают текущий день
                    const obj = data.find(item => day >= item.startDate && day <= item.endDate);
                    // Если объект найден, возвращаем его интенсивность, иначе 0
                    return obj ? obj.intensityIndex : 0;
                });

// Обновляем данные графика
                chart2.data.labels = labels;
                chart2.data.datasets[0].data = chartData;
                chart2.update();
            });

    }


    // Отрисовываем график на странице с помощью Chart.js
    const chart1 = new Chart('chart1', {
        type: 'line',
        data: {
            labels: Object.keys(data).sort(),
            datasets: [{
                label: 'Выручка по дням',
                data: Object.values(data).sort(),
                borderColor: 'rgb(255, 99, 132)',
                borderWidth: 1
            }]
        },
        options: {
            legend: {
                display: false
            },
            responsive: false,
            scales: {
                x: {
                    title: {
                        display: true,
                        text: 'День'
                    }
                },
                y: {
                    title: {
                        display: true,
                        text: 'Сумма'
                    },
                    beginAtZero: true
                }
            },
            // plugins: {
            //     title: {
            //         display: true,
            //         text: 'Выручка по дням'
            //     }
            // }
        }
    });
    // Отрисовываем график на странице с помощью Chart.js
    const chart2 = new Chart('chart2', {
        type: 'bar',
        data: {
            labels: [],
            datasets: [{
                label: 'Интенсивность рекламы',
                data: [],
                borderColor: 'rgb(255, 99, 132)',
                borderWidth: 1
            }]
        },
        options: {
            legend: {
                display: false
            },
            responsive: false,
            scales: {
                x: {
                    title: {
                        display: true,
                        text: 'День'
                    }
                },
                y: {
                    min: 0,
                    max: 8,
                    stepSize: 1,
                    title: {
                        display: true,
                        text: 'Интенсивность'
                    },
                    beginAtZero: true
                }
            },

        }
    });
    // plugins: {
    //     title: {
    //         display: true,
    //         text: 'Выручка по дням'
    //     }
    // }

    <#--function showTab3Charts() {-->
    <#--    const charts = {};-->
    <#--    <#list competitors as competitor>-->
    <#--    const chartt${competitor.id} = new Chart('chartt${competitor.id}', {-->
    <#--        type: 'line',-->
    <#--        data: {-->
    <#--            labels: [],-->
    <#--            datasets: [{-->
    <#--                label: 'Выручка по дням',-->
    <#--                data: [],-->
    <#--                borderColor: 'rgb(255, 99, 132)',-->
    <#--                borderWidth: 1-->
    <#--            }]-->
    <#--        },-->
    <#--        options: {-->
    <#--            legend: {-->
    <#--                display: false-->
    <#--            },-->
    <#--            responsive: false,-->
    <#--            scales: {-->
    <#--                x: {-->
    <#--                    title: {-->
    <#--                        display: true,-->
    <#--                        text: 'День'-->
    <#--                    }-->
    <#--                },-->
    <#--                y: {-->
    <#--                    title: {-->
    <#--                        display: true,-->
    <#--                        text: 'Сумма'-->
    <#--                    },-->
    <#--                    beginAtZero: true-->
    <#--                }-->
    <#--            }-->
    <#--        }-->
    <#--    });-->
    <#--    charts['${competitor.id}'] = chartt${competitor.id};-->
    <#--    getChartDataFromItem(${competitor.id}, chartt${competitor.id});-->
    <#--    </#list>-->

    <#--    function updateCharts() {-->
    <#--        <#list competitors as competitor>-->
    <#--        getChartDataFromItem(${competitor.id}, charts['${competitor.id}']);-->
    <#--        </#list>-->
    <#--    }-->
    <#--}-->


    let charts = {};
    <#list competitors as competitor>
    const chartt${competitor.id} = new Chart('chartt${competitor.id}', {
        type: 'line',
        data: {
            labels: [],
            datasets: [{
                label: 'Выручка по дням',
                data: [],
                borderColor: 'rgb(255, 99, 132)',
                borderWidth: 1
            }]
        },
        options: {
            legend: {
                display: false
            },
            responsive: false,
            scales: {
                x: {
                    title: {
                        display: true,
                        text: 'День'
                    }
                },
                y: {
                    title: {
                        display: true,
                        text: 'Сумма'
                    },
                    beginAtZero: true
                }
            }
        }
    });
    charts['${competitor.id}'] = chartt${competitor.id};
    getChartDataFromItem(${competitor.id}, chartt${competitor.id});
    </#list>

    function getChartDataFromItem(competitorId, chart) {
        fetch('http://localhost:80/game/revenue-data/' + competitorId)
            .then(response => response.json())
            .then(newData => {
                // создаем массив данных для графика
                let chartData = [];
                // определяем максимальный ключ в мапе
                let maxKey = Math.max.apply(Math, Object.keys(newData));
                const labels = Array.from({length: maxKey}, (_, i) => i + 1);
                // проходим циклом по всем возможным ключам
                for (let i = 1; i <= maxKey; i++) {
                    // если значение для ключа существует в мапе, добавляем его в массив данных
                    if (i in newData) {
                        chartData.push(newData[i]);
                    } else { // иначе добавляем ноль
                        chartData.push(0);
                    }
                }
                // Обновляем график
                chart.data.labels = labels;
                chart.data.datasets[0].data = chartData;
                chart.update();
            });
    }

    function updateCharts() {
        <#list competitors as competitor>
        getChartDataFromItem(${competitor.id}, charts['${competitor.id}']);
        </#list>
    }


    document.getElementById('refreshTab1').addEventListener('click', fetchBalanceAndDebt);
    document.getElementById('refreshTab2').addEventListener('click', fetchData);
    document.getElementById('myTab').addEventListener('shown.bs.tab', function (event) {
        const targetTabId = event.target.getAttribute('aria-controls');
        switch (targetTabId) {
            case 'tab1':
                fetchBalanceAndDebt();
                document.getElementById('tab1-image').style.display = 'block';
                document.getElementById('tab2-image').style.display = 'none';
                break;
            case 'tab2':
                fetchData();
                document.getElementById('tab1-image').style.display = 'none';
                // document.getElementById('tab2-image').style.display = 'block';
                break;
            case 'tab3':
            <#list competitors as competitor>
                getChartDataFromItem(${competitor.id}, charts['${competitor.id}']);
            </#list>
                break;
            default:
                break;
        }
    });

    // Загрузить данные при загрузке страницы
    fetchBalanceAndDebt();


    document.getElementById('tab1-image').style.display = 'block';
    // document.getElementById('tab2-image').style.display = 'none';
</script>
</body>
</html>