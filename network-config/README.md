network-config
==============

`network-config` is a project manage dynamic changing configurations.

* configurations managed by config group.
* every config group can be assigned read auth to any user.
* every config group contains lots of config items.
* every config item is used by end-user.

h2. user archtecture.

There are four types of users.
* ADMIN the super user, can do everything.
* OP can add NODE user, can add, read, update config. change password for others.
* USER can update config, can change password of itself.
* NODE used by machine, can read config.
