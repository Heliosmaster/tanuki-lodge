# tanuki-lodge

- Tanuki is the mascot of [GitLab](https://gitlab.org) (as well as an [interesting animal](https://www.tofugu.com/japan/tanuki/) )
- [Clubhouse](https://clubhouse.io) is a great project management website

As of September 2018 there is not yet integration between Clubhouse and GitLab.

I wanted to control the status of stories (and more) through commits, so I wrote this piece of software which is essentially a bridge between [GitLab webhooks](https://docs.gitlab.com/ce/user/project/integrations/webhooks.html) and [Clubhouse REST APIs](https://clubhouse.io/api/rest/v2/)

## What works

You reference stories using the tag `[chX]` where `X` is the story identifier (a number)

If you reference a story in the description of a merge request, the following things will happen:

- Open merge requests with the `WIP` status --> In Progress
- Open merge request --> Review
- Merged merge request --> To be deployed


## Running the program

```
lein uberjar
```

it will create a JAR file in `target/tanuki-lodge.jar`. Simply execute that jar passing the following environment variables:

* `token`: the clubhouse API token
* `server-port`: in which to run the webserver

so, for example, run it as

`java -Dtoken="XXX" -Dserver.port=400 -jar /path/to/tanuki-lodge.jar`
