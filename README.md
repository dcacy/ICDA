# ICDA

This is a modification to the very excellent work done by Paul Godby
as a straightforward way to import data to, and export data from, Connections.

Please do not submit pull requests or issues to this repository, as it is not being monitored or supported. As the [License](LICENSE) indicates, this is sample code to help you understand the power and limits of the Connections API.

## Getting started

1. Add the following libraries to the `lib` directory or otherwise include them in the build/run path. Note: these are the versions of the files which are known to work:

```none
  abdera-1.1.3.jar  
  abdera-client-1.1.3.jar  
  abdera-core-1.1.3.jar
  abdera-parser-1.1.3.jar
  axiom-api-1.2.14.jar
  axiom-impl-1.2.14.jar
  commons-codec-1.4.jar
  commons-httpclient-3.1.jar
  commons-io-2.4.jar
  commons-lang-2.4.jar
  commons-logging-1.0.4.jar
  jaxen-1.1.1.jar
```

2. Compile/run with a Java 1.6 JRE.

 this is a line
 and thie is 
  and tanother  
  third line

3. Edit the `icda.properties` file.

4. Run with the following command: 


  ```
  java -jar ICDA.jar import|export|delete
  ```

## Important Caveats

This tool was created to easily export data from one instance of Connections and import into another, to make 
it easier for the IBM Collaboration Solutions team to create demos. It assumes that:

- Users are not federated, and
- You know the IDs and passwords of the users who "create" the content

This application was not designed for production use, so there are absolutely NO GUARANTEES it will do anything good, and it may do bad things. Please read the [License](LICENSE).

This application is designed to export and import Communities, not standalone content. However, it will not export or import _everything_ in your Communities. It is not a full-featured migration tool because it uses the Connections APIs, which means:

- Some features are unavailable because there is no API for that feature (ex. votes in an Ideation Blog because those are supposed to be private).
- You cannot use the API to create content on behalf of someone else, which is why you must know the ID and password of the content creator.
- You cannot manipulate the timestamp of content you create.
- If you are exporting content which has links to other Connections content, those links will be broken when you import into a new Community because every Connections artifact has its own ID and that ID is part of the link.

## Supported runtimes

IBM JRE 1.6, Oracle JRE 1.6+ are supported. IBM JRE 1.7+ are not supported because of an incompatibility with the version of the Axiom XML library used by Abdera.

## How it works

### Properties file

The tool uses a properties file called **icda.properties**. Set `connections.url` to point to the URL of the Connections server.

```none
connections.url=https://connections.ibm.com
```

#### Login details

The application only supports Basic Authentication. This means that if your Connections server has federated users, **it will not work**. For an on premises Connections server which is not federated, set `auth.login.attr` to `uid`; for a Connections Cloud instance, use `mail`.

```
auth.type=basic
auth.login.attr=uid
```

#### Content files

The **data** directory contains the files to be used for export or import. The properties file should point to them:

```
file.data.import=import.xml
file.data.export=export.xml
```

#### Exporting Communities

If you export Communities for a user, the application will by default export all Communities owned by that user. To export only one Community, indicate that in the properties file:

```none
export.community.single=true
export.community.uUid=9f0dae33-77e1-4946-af81-ba874b6a5d30
export.community.owner.uid=fadams
```

#### Users

The property file points to the XML file in the **data** directory which contains information about the users in your environment.

```
file.data.users=greenwell_users.xml
```

Each user should have a `uid` attribute (for on premises Connections) and/or a `mail` attribute (for Connections Cloud).

```
<user uid="lingshin" mail="lingshin@greenwell.com" password="UseYourPassword" />
```

### Scenarios

#### Export

The users file is where you give instructions on what you want to export. To export all communities for all users in the users file, use an asterisk:

```XML
<export>
  <activities></activities>
  <blogs></blogs>
  <bookmarks></bookmarks>
  <communities>*</communities>
  <files></files>
  <forums></forums>
  <profiles></profiles>
  <wikis></wikis>
</export>
```

To export communities created by a specific set of users, provide a comma-separated list of uids:

```XML
<communities>dmisawa,fadams</communities>
```

Note: these settings are overridden by setting the `export.community` values in **icda.properties** (see above).

When you run the export, an XML file will be created in the **data** directory containing the exported data. Any files downloaded will be in the **files** directory. **Existing files will be overwritten.**

Run the application with:
```
java -jar ICDA.jar export
```


#### Import

To import, use the users file just as you do for exporting (see above). An asterisk will load all data found in the import file for that application, and providing a uid will limit the import to that user.

Run the application with:
```none
java -jar ICDA.jar import
```


#### Delete

Deleting is done by configuring the users file similarly to what is described above for exporting.

Run the application with:
```
java -jar ICDA.jar delete
```

