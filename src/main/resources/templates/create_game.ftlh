<!DOCTYPE html>
<head xmlns="http://www.w3.org/1999/html">
    <title>Создание игры</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0-alpha1/dist/css/bootstrap.min.css" rel="stylesheet"
          integrity="sha384-GLhlTQ8iRABdZLl6O3oVMWSktQOp6b7In1Zl3/Jr59b6EGGoI1aFkw7cmDA6j6gD" crossorigin="anonymous">
    <style>
        .divider:after,
        .divider:before {
            content: "";
            flex: 1;
            height: 1px;
            background: #eee;
        }

        .h-custom {
            height: calc(100% - 73px);
        }

        @media (max-width: 450px) {
            .h-custom {
                height: 100%;
            }
        }
    </style>
</head>
<body>
<section class="vh-100">
    <div class="container-fluid h-custom">
        <div class="row d-flex justify-content-center align-items-center h-100">
            <div class="col-md-8 col-lg-6 col-xl-4 offset-xl-1">
                <form action="/game" method="post">
                    <div class="d-flex flex-row align-items-center justify-content-center justify-content-lg-start mb-3">
                        <h2 class="fw-normal mb-0 me-3">Да начнется новая игра!</h2>
                    </div>

                    <div class="form-outline mb-4">
<#--                        <label for="name">Наименование игры</label>-->
                        <input class="form-control form-control-lg" type="text" id="name" name="name" placeholder="Наименование игры">
                    </div>

                    <div class="form-outline mb-4">
                        <input class="form-control form-control-lg" type="number" id="timeUnit" name="timeUnit"
                               placeholder="Модельное время в минутах(сутки = ? минут)">
                    </div>

                    <div class="form-outline mb-4">
                        <input class="form-control form-control-lg" type="number" step="any"
                               name="interestRateInvestmentCredit" placeholder="Процентная ставка инвестиционного кредита">
                    </div>

                    <div class="form-outline mb-4">
                        <input class="form-control form-control-lg" type="number" step="any"
                               name="interestRateBusinessCredit" placeholder="Процентная ставка бизнес-кредита">
                    </div>

                    <div class="form-outline mb-4">
                        <input class="form-control form-control-lg" type="number" step="any" name="salesTax"
                               placeholder="Процент налога с продаж">
                    </div>

                    <div class="form-outline mb-4">
                        <input class="form-control form-control-lg" type="number" step="any" name="baseCostPrice"
                               placeholder="Себестоимость единицы продукции">
                    </div>

                    <div class="form-outline mb-4">
                        <input class="form-control form-control-lg" type="number" step="any" name="productPower"
                               placeholder="Сумма средств, необходимых для производства еще одной единицы продукции">
                    </div>

                    <div class="form-outline mb-4">
                        <input class="form-control form-control-lg" type="number" step="any" name="baseAdvertisementPrice"
                               placeholder="Цена рекламы">
                    </div>

                    <div class="form-outline mb-4">
                        <input class="form-control form-control-lg" type="number" name="requiredQuantity"
                               placeholder="Спрос покупателя(необходимое количество продукции)">
                    </div>

                    <div class="form-outline mb-3">
                        <p class="lead">Выберите участников игры из списка:</p>
                        <select id="accounts" class="form-control form-control-lg" multiple name="accountIds">
                            <#list accounts as account>
                                <option value="${account.id}">${account.fullName!}</option>
                            </#list>
                        </select>
                    </div>

                    <div class="text-center text-lg-start mt-10 pt-4">
                        <label>
                            <input class="btn btn-primary btn-lg"
                                   style="padding-left: 2.5rem; padding-right: 2.5rem;" type="submit" value="Создать">
                        </label>
                    </div>

                </form>
            </div>
            <div class="col-md-9 col-lg-6 col-xl-5">
                <#--                <img src="https://mdbcdn.b-cdn.net/img/Photos/new-templates/bootstrap-login-form/draw2.webp"-->
                <#--                     class="img-fluid" alt="Sample image">-->
<#--                <img src="https://www.advgazeta.ru/upload/iblock/42d/kak_predprinimatelyam_nachat_2023_god.jpg"-->
<#--                     class="img-fluid" alt="Sample image">-->
                <#--                <img src="https://mdbcdn.b-cdn.net/img/Photos/new-templates/bootstrap-login-form/draw2.svg"-->
                <#--                     class="img-fluid" alt="Sample image">-->
                                <img src="https://sun9-9.userapi.com/impg/M6YIz5fS_4YMoiHLVzrebf_YFdge0aNdmDF1vg/Vf1Cc5-ixmw.jpg?size=1087x840&quality=96&sign=d607b47217efe3197ce2dd2e9b8ca53e&type=album"
                                     class="img-fluid" alt="Sample image">
            </div>
        </div>
    </div>
    <div
            class="d-flex flex-column flex-md-row text-center text-md-start justify-content-between py-4 px-4 px-xl-5 bg-primary">
        <!-- Copyright -->
        <div class="text-white mb-3">
            Copyright © 2023. All rights reserved.
        </div>
        <!-- Copyright -->
    </div>
</section>

<#--<h2>Новая игра:</h2>-->
<#--<form action="/game" method="post">-->
<#--    <div class="form-group">-->
<#--        <label for="timeUnit">Модельное время в минутах(сутки = ? минут)</label>-->
<#--        <input class="form-control" type="number" id="timeUnit" name="timeUnit">-->
<#--    </div>-->
<#--    <div class="form-group">-->
<#--        <label for="interestRateInvestmentCredit">Процентная ставка инвестиционного кредита</label>-->
<#--        <input class="form-control" type="number" step="any" name="interestRateInvestmentCredit" placeholder="">-->
<#--    </div>-->
<#--    <div class="form-group">-->
<#--        <label for="interestRateBusinessCredit">Процентная ставка бизнес-кредита</label>-->
<#--        <input class="form-control" type="number" step="any" name="interestRateBusinessCredit" placeholder="">-->
<#--    </div>-->
<#--    <div class="form-group">-->
<#--        <label for="salesTax">Процент налога с продаж</label>-->
<#--        <input class="form-control" type="number" step="any" name="salesTax">-->
<#--    </div>-->
<#--    <div class="form-group">-->
<#--        <label for="salesTax">Себестоимость единицы продукции</label>-->
<#--        <input class="form-control" type="number" step="any" name="baseCostPrice">-->
<#--    </div>-->
<#--    <div class="form-group">-->
<#--        <label for="productPower">Сумма средств, необходимых для производства еще одной единицы продукции</label>-->
<#--        <input class="form-control" type="number" step="any" name="productPower">-->
<#--    </div>-->
<#--    <div class="form-group">-->
<#--        <label for="accounts">Участники игры</label>-->
<#--        <select id="accounts" class="form-control" multiple name="accountIds">-->
<#--            <#list accounts as account>-->
<#--                <option value="${account.id}">${account.fullName}</option>-->
<#--            </#list>-->
<#--        </select>-->
<#--    </div>-->
<#--    <div class="form-group">-->
<#--        <input class="btn btn-primary btn-lg"-->
<#--               style="padding-left: 2.5rem; padding-right: 2.5rem;" type="submit" value="Создать">-->
<#--    </div>-->
<#--</form>-->

<#--<form action="/game" method="post">-->
<#--    <input type="number" name="timeUnit" placeholder="Модельное время в минутах(сутки = ? минут)">-->
<#--    <input type="number" step="any" name="interestRateInvestmentCredit" placeholder="">-->
<#--    <input type="number" step="any" name="interestRateBusinessCredit" placeholder="">-->
<#--    <input type="number" step="any" name="salesTax" placeholder="Процент налога с продаж">-->
<#--    <input type="number" step="any" name="baseCostPrice" placeholder="Себестоимость единицы продукции">-->
<#--    <input type="number" step="any" name="productPower" placeholder="">-->
<#--    <select id="accounts" multiple name="accountIds">-->
<#--        <#list accounts as account>-->
<#--            <option value="${account.id}">${account.fullName}</option>-->
<#--        </#list>-->
<#--    </select>-->
<#--    <input type="submit" value="Создать">-->
<#--</form>-->
</body>
</html>