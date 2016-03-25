'use strict';

angular.module('silq2App')
    .controller('GrupoDetailController', function ($scope, $stateParams, entity, Grupo, Upload, Flash) {
        $scope.grupo = entity;
        $scope.load = function (id) {
            Grupo.get({id: id}, function(result) {
                $scope.grupo = result;
            });
        };

        $scope.files = [];
        $scope.uploadConfig = {
            url: 'api/grupos/' + $stateParams.id + '/addPesquisador'
        };

        $scope.uploaded = function(resp) {
            $scope.grupo.pesquisadores.push(resp.data);
            Flash.create('success', '<strong>Sucesso!</strong> Pesquisador(es) adicionado(s)');
        };

        $scope.removePesquisador = function(pesquisador) {
            Grupo.removePesquisador($stateParams.id, pesquisador.id).then(function() {
                var index = $scope.grupo.pesquisadores.indexOf(pesquisador);
                index != -1 && $scope.grupo.pesquisadores.splice(index, 1);
                Flash.create('success', '<strong>Sucesso!</strong> O pesquisador "'+pesquisador.nome+'" foi removido do grupo.');
            }).catch(function(err) {
                console.error(err);
            });
        };
    });
