'use strict';

angular.module('silq2App')
    .config(function ($stateProvider) {
        $stateProvider
            .state('home', {
                parent: 'site',
                url: '/',
                data: {
                    authorities: [],
                    pageTitle: 'SILQ - Sistema de Integração Lattes - Qualis'
                },
                views: {
                    'content@': {
                        templateUrl: 'scripts/app/main/main.html',
                        controller: 'MainController'
                    }
                },
                resolve: {

                }
            })
            .state('about', {
                parent: 'site',
                url: '/about',
                data: {
                    authorities: [],
                    pageTitle: 'Sobre'
                },
                views: {
                    'content@': {
                        templateUrl: 'scripts/app/main/about.html'
                    }
                },
                resolve: {

                }
            })
            .state('contact', {
                parent: 'site',
                url: '/contact',
                data: {
                    authorities: [],
                    pageTitle: 'Contato'
                },
                views: {
                    'content@': {
                        templateUrl: 'scripts/app/main/contact.html'
                    }
                },
                resolve: {

                }
            });
    });
