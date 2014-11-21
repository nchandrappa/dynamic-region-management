module.exports = function(grunt) {
  function startServer(number) {
    return 'cd tmp/gemfire && gfsh run --file /vagrant/bin/startServer' + number + '.gfsh';
  }

  function ensureServerRunning(number) {
    return 'test -e tmp/gemfire/server' + number + '/vf.gf.server.pid && ps ax | grep `cat tmp/gemfire/server' + number + '/vf.gf.server.pid` | grep -qv grep && echo "Server ' + number + ' already running..." || (' + startServer(number) + ')';
  }


  var startLocator = 'cd tmp/gemfire && gfsh run --file /vagrant/bin/startLocator.gfsh';
  var ensureLocatorRunning = '(test -e tmp/gemfire/locator/vf.gf.locator.pid && ps ax | grep `cat tmp/gemfire/locator/vf.gf.locator.pid` | grep -qv grep && echo "Locator already running...") || (' + startLocator + ')';

  grunt.initConfig(
    {
      pkg: grunt.file.readJSON('package.json'),
      shell: {
        ensureServer1Running: {
          command: ensureServerRunning(1)
        },
        ensureServer2Running: {
          command: ensureServerRunning(2)
        },
        startServer1: {
          command: startServer(1)
        },
        startServer2: {
          command: startServer(2)
        },
        stopServer1: {
          command: 'cd tmp/gemfire && gfsh run --file /vagrant/bin/stopServer1.gfsh'
        },
        stopServer2: {
          command: 'cd tmp/gemfire && gfsh run --file /vagrant/bin/stopServer2.gfsh'
        },
        ensureLocatorRunning: {
          command: ensureLocatorRunning
        },
        startLocator: {
          command: startLocator
        },
        stopLocator: {
          command: 'cd tmp/gemfire && gfsh run --file /vagrant/bin/stopLocator.gfsh'
        },
        buildJavaFunctions: {
          command: 'cd java && ./gradlew clean build',
          src: [
            "java/src/**/*.java",
            "java/build.gradle",
          ]
        },
        deployJavaFunctions: {
          command: 'grunt servers:restart',
          src: [
            'java/build/libs/java.jar'
          ]
        },
        jasmine: {
          command: 'node_modules/.bin/jasmine'
        },
        junit: {
          command: 'cd java && ./gradlew test',
        }
      },
    }
  );

  grunt.loadNpmTasks('grunt-shell');
  grunt.loadNpmTasks('grunt-newer');

  grunt.registerTask('server1:start', ['locator:ensure', 'shell:startServer1']);
  grunt.registerTask('server1:stop', ['shell:stopServer1']);
  grunt.registerTask('server1:restart', ['server1:stop', 'server1:start']);
  grunt.registerTask('server1:ensure', ['locator:ensure', 'shell:ensureServer1Running']);

  grunt.registerTask('server2:start', ['locator:ensure', 'shell:startServer2']);
  grunt.registerTask('server2:stop', ['shell:stopServer2']);
  grunt.registerTask('server2:restart', ['server2:stop', 'server2:start']);
  grunt.registerTask('server2:ensure', ['locator:ensure', 'shell:ensureServer2Running']);

  grunt.registerTask('servers:start', ['server1:start', 'server2:start']);
  grunt.registerTask('servers:stop', ['server1:stop', 'server2:stop']);
  grunt.registerTask('servers:restart', ['servers:stop', 'servers:start']);
  grunt.registerTask('servers:ensure', ['server1:ensure', 'server2:ensure']);

  grunt.registerTask('locator:start', ['shell:startLocator']);
  grunt.registerTask('locator:stop', ['servers:stop', 'shell:stopLocator']);
  grunt.registerTask('locator:restart', ['locator:stop', 'locator:start', 'servers:start']);
  grunt.registerTask('locator:ensure', ['shell:ensureLocatorRunning']);

  grunt.registerTask('java:build', ['newer:shell:buildJavaFunctions']);
  grunt.registerTask('java:deploy', ['newer:shell:deployJavaFunctions']);
  grunt.registerTask('java:ensure', ['java:build', 'java:deploy']);

  grunt.registerTask('setup', ['locator:ensure', 'java:ensure', 'servers:ensure']);

  grunt.registerTask('jasmine', ['setup', 'shell:jasmine']);
  grunt.registerTask('junit', ['shell:junit']);
  grunt.registerTask('test', ['jasmine']);

  grunt.registerTask('default', ['test']);
};
