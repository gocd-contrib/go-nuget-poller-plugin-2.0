NuGet Poller Plugin for Go
==================================

Introduction
------------
This is a [package material](https://docs.go.cd/current/extension_points/package_repository_extension.html) plugin for [Go](http://www.thoughtworks.com/products/go-continuous-delivery). It is currently capable of polling [NuGet](http://www.nuget.org/) repositories running [API V2](http://chris.eldredge.io/blog/2013/02/25/fun-with-nuget-rest-api/).

The behaviour and capabilities of the plugin is determined to a significant extent by that of the package material extension point in Go. Be sure to read the package material documentation before using this plugin.

This is a pure Java plugin. It does not need nuget.exe. You may however require nuget.exe on the agents.

Installation
------------
Just drop [go-nuget-poller.jar](https://github.com/gocd-contrib/go-nuget-poller-plugin-2.0/releases) into plugins/external directory and restart Go. More details [here](http://www.thoughtworks.com/products/docs/go/13.3/help/plugin_user_guide.html)

Compatibility
------------
This plugin is compatible with the JSON message based plugin API introduced in version 14.4.0. More details [here](https://developer.go.cd/16.3.0/writing_go_plugins/json_message_based_plugin_api.html)

Repository definition
---------------------
![Add a NuGet repository][1]

NuGet Server URL must be a valid http or https URL. For example, to add nuget.org as a repository, specify the URL as http://nuget.org/api/v2. The plugin will try to access URL$metadata to report successful connection. Basic authentication (user:password@host/path) is supported. We recommend only using authentication over HTTPS.

Package definition
------------------
Click check package to make sure the plugin understands what you are looking for. Note that the version constraints are ANDed if both are specified.

![Define a package as material for a pipeline][2]

[1]: img/add-nuget-repo.png  "Define NuGet Package Repository"
[2]: img/add-nuget-package.png  "Define package as material for a pipeline"
