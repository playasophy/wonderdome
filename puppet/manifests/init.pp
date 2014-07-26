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
# [*java_opts*]
#   Options to pass to Java when running the service.
#
class wonderdome (
  $user        = 'wonder',
  $service     = 'wonderdome',
  $server_name = 'wonderdome',
  $server_port = 8080,
  $java_opts   = "-server",
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


  ### SERVICE CONFIG ###

  $service_config = "${home}/config.clj"
  file { $service_config:
    ensure  => file,
    content => template('wonderdome/config.clj.erb'),
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
    require => File[$service_config],
  }

  service { $service:
    ensure    => running,
    enable    => true,
    subscribe => File[$service_config,
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
