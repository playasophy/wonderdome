# == Class: wonderdome
#
# Installs the wonderdome software.
#
# === Parameters
#
# [*user*]
#   Name of service user. Defaults to 'wonder'.
#
# [*service*]
#   Name of the service process.
#
# [*server_name*]
#   Hostname to serve website as.
#
# [*server_port*]
#   Port to serve the website on locally. The external port will always be the
#   standard HTTP port 80.
#
# [*java_heap*]
#   Size to assign to the JVM heap. Defaults to 1 GB ("1024M").
#
# [*java_opts*]
#   Additional options to pass to Java when running the service.
#
class wonderdome (
  $user        = 'wonder',
  $service     = 'wonderdome',
  $server_name = 'wonderdome',
  $server_port = 8080,
  $java_heap   = '1024M',
  $java_opts   = [
    '-server',
    '-XX:+UseConcMarkSweepGC',
    '-XX:+UseParNewGC',
    '-XX:+CMSIncrementalPacing',
    '-XX:ParallelGCThreads=2',
    '-XX:+AggressiveOpts',
    #'-XX:+PrintGC'
    #'-Dcom.sun.management.jmxremote.port=1098',
    #'-Dcom.sun.management.jmxremote.authenticate=false',
    #'-Dcom.sun.management.jmxremote.ssl=false',
  ],
) {

  ### SYSTEM CONFIG ###

  $packages = [
    # TODO: currently using oracle's embedded arm JRE
    #'openjdk-7-jre-headless',
  ]

  package { $packages:
    ensure   => installed,
    provider => apt,
  }

  file { '/etc/udev/rules.d/70-gamepad.rules':
    ensure => file,
    source => 'puppet:///modules/wonderdome/udev-gamepad.rules',
    owner  => 'root',
    group  => 'root',
    mode   => '0644',
  }



  ### SERVICE USER ###

  $home = "/srv/${user}"

  user { $user:
    ensure => present,
    uid    => 888,
    gid    => $user,
    groups => ['audio', 'crontab', 'plugdev'],
    home   => $home,
  }

  file { $home:
    ensure => directory,
    owner  => $user,
    group  => $user,
    mode   => '0644',
  }

  # TODO: add ssh-keys to ~wonder/.ssh/authorized_keys

  # TODO: add sudoers line to allow wonder to restart its own service without a password
  # wonder  ALL=(root) NOPASSWD:/usr/sbin/service wonderdome *



  ### LOGGING CONFIG ###

  $log_dir = "/var/log/wonderdome"
  $log_file = "${log_dir}/system.log"

  file { $log_dir:
    ensure  => directory,
    owner   => $user,
    group   => $user,
    mode    => '0644',
    require => User[$user],
  }



  ### SERVICE CONFIG ###

  $java_lib_path = "${home}/lib"

  $service_jar = "${home}/wonderdome.jar"
  file { $service_jar:
    ensure  => file,
    owner   => $user,
    group   => $user,
    mode    => '0644',
    require => File[$home],
  }

  $service_config = "${home}/config.clj"
  file { $service_config:
    ensure  => file,
    owner   => $user,
    group   => $user,
    mode    => '0644',
    require => File[$home],
  }

  $upstart_config = "/etc/init/${service}.conf"
  file { $upstart_config:
    ensure  => file,
    content => template('wonderdome/upstart.conf.erb'),
    owner   => 'root',
    group   => 'root',
    mode    => '0644',
    require => File[$service_jar,
                    $service_config],
  }

  service { $service:
    ensure    => running,
    enable    => true,
    subscribe => File[$service_jar,
                      $service_config,
                      $upstart_config],
  }



  ### NGINX REVERSE PROXY ###

  include nginx

  $upstream_server = 'wonder-web'
  nginx::resource::upstream { $upstream_server:
    members => [
      "localhost:${server_port}"
    ],
  }

  nginx::resource::vhost { $server_name:
    proxy   => "http://${upstream_server}",
    require => Service[$service],
  }

}
