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

it will create a JAR file in `target/tanuki-lodge.jar`.


Create a configuration file in EDN named `config.edn`, such as:

```
{:token "YOUR_CLUBHOUSE_TOKEN_HERE"
 :server-port "1234"
 :merge-request-open-wip "In progress"
 :merge-request-open "Review"
 :merge-request-merged "To be Deployed"}
```

where the last few options are the names of the state in which you want to move the stories referenced in

* `merge-request-open-wip`: **Open** and **Work In Progress** Merge request
* `merge-request-open`: **Open** merge request
* `merge-request-merged`: **Merged** merge request

Then simply execute the jar file passing the path of the `config.edn` file you just created as first command-line argument.

`java -jar /path/to/tanuki-lodge.jar /path/to/config.edn`

At startup, the configuration file will be printed to `stdout` so you can make sure it was loaded correctly.