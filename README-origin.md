# rxnorm-data

### Team Ownership - Product Owner
Data Team

## Getting Started

Follow these instructions to generate a rxnorm data ORIGIN dataset:

1. Clone the [rxnorm-data repository](https://github.com/ikmdev/rxnorm-data)

```bash
git clone [Rep URL]
```

2. Change local directory to `rxnorm-data\rxnorm-origin`

3. Ensure the rxnorm-data/pom.xml contains the proper tags containing source filename for the files such as:
   <source.zip>, <source.version>, etc.

4. Enter the following command to build the ORIGIN dataset:

```bash
mvn clean install -U "-DMaven.build.cache.enable=false"
```

5. Enter the following command to deploy the ORIGIN dataset to Nexus:

```bash
mvn deploy -f rxnorm-origin -DdeployToNexus=true -Dmaven.deploy.skip=true -Dmaven.build.cache.enabled=false -Ptinkarbuild -DrepositoryId=nexus-snapshot
```

6. On Nexus, you will find the artifact at the following maven coordinates:

```bash
<dependency>
  <groupId>dev.ikm.data.rxnorm</groupId>
  <artifactId>rxnorm-origin</artifactId>
  <version>Pilot-Defined-RxNorm-with-SNCT-classes-2024-04-10-with-custom-annotations.owl</version>
</dependency>
```
