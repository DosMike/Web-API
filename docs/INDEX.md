# Web-API documentation & tutorials

This is a collection of various documentations and tutorials for various sections of the Web-API.

1. [AdminPanel](https://github.com/Valandur/admin-panel)  
More information about the AdminPanel and how to set it up.

1. [Commands (**and their permissions**)](COMMANDS.md)  
Lists the various commands the Web-API provides, including their required player **permissions**.

1. [Routes](https://valandur.github.io/Web-API/redoc.html)  
Web-API automatically hosts the documentation at http://localhost:8080/docs (by default) 
on your minecraft server for you to explore. You can find the same documentation [here](https://valandur.github.io/Web-API/redoc.html)

1. [Consumer Tutorial](CONSUME.md)  
This tutorial shows some basic examples of how to use/consume the Web-API. This will be helpfull
if you're building an app that relies on the data from the Web-API

1. [General config](CONFIG.md)  
This sections describes the basic configuration options of the Web-API.

1. [Permissions (for API endpoints, **not** commands)](PERMISSIONS.md)  
This documentation talks about the `permissions.conf` file and the various settings you can use
to adjust the Web-API to your needs, specifically the permissions that restrict access and data.

1. [Messages](MESSAGE.md)  
Read this guide to find out how to send messages with interactive options to players and 
process their responses.

1. [WebHooks](WEBHOOKS.md)  
This documentation explains how to use WebHooks to have your own app stay informed about stuff
that happens on the minecraft server, without constantly requesting data.

1. [WebHook Filters](WEBHOOKS_FILTERS.md)  
WebHook filters can be used to filter out certain web hook events and greatly reduce the amount
of events that are sent to your server. Read the [WebHook documentation](WEBHOOKS.md) first.

1. [Additional Data](DATA.md)  
This documentation talks about additional data that is included with player, world and entity
endpoints, as well as the implementation progress of further data which is not yet supported.

1. [API](API.md)  
This documentation talks about the Web-API API, which other plugins may use to create their 
own endpoints and expose data easily.
