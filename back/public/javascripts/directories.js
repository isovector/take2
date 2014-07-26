var directories = angular.module('directories', ['ui.listview'])
.controller('DirecCtrl', ['$scope', function($scope) {
  $scope.items = [
    {
      filename: "back",
      filesize: "45678",
      editedlast: new Date(),
      isDirec: true,
      direcs: "back",
      thumb: 'back'
    },
    {
      filename: "README",
      filesize: "456",
      editedlast: new Date(),
      isDirec: false,
      direcs: null,
      thumb: 'README'
    }]

  function humanFileSize(bytes, si) {
    var thresh = si ? 1000 : 1024;
    if(bytes < thresh) return bytes + ' B';
    var units = si ? ['KB','MB','GB','TB','PB','EB','ZB','YB'] : ['KiB','MiB','GiB','TiB','PiB','EiB','ZiB','YiB'];
    var u = -1;
    do {
      bytes /= thresh;
      ++u;
    } while(bytes >= thresh);
    return bytes.toFixed(0)+' '+units[u];
  }

  function formatDate(dateStr) {
    var datetime = new Date(dateStr);
    var year = datetime.getFullYear();
    var month = datetime.getMonth();
    var date = datetime.getDate();
    var hours = datetime.getHours();
    var minutes = datetime.getMinutes();

    var ampm = hours >= 12 ? 'PM' : 'AM';
    hours = hours % 12;
    hours = hours ? hours : 12;
    minutes = minutes < 10 ? '0'+minutes : minutes;

    return date + '/' + month + '/' + year + ' ' + hours + ':' + minutes + ampm;
  }
  
  function thumb(item){
    return "0.0.0.0/directory/" + item.thumb
  }

  function filename(item) {
    return item.filename
  }

  $scope.listview = {}
  $scope.listview.methods = {
    filename: filename,
    editedlast: formatDate,
    filesize: humanFileSize,
    thumb: thumb,
  }


}]);
