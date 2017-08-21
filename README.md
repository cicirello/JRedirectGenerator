# JRedirectGenerator

Copyright (C) 2017 Vincent A. Cicirello.

http://www.cicirello.org/

Command line utility for generating simple html files for redirecting old urls to new.  The intended use of this utility is for those who do not have sufficient access to otherwise generate 301 redirects.

If you use the provided default redirect page template, in the file `redirect.template`, then each generated html file:

- uses a meta refresh directive to redirect to the new url, 

- has a robots noindex directive, which will hopefully have the effect of
     causing the old url to drop out of search results 

- includes a canonical link pointing to the new url

- body with a simple javascript redirect in case user's browser doesn't follow the meta refresh

- body also has a link to click in case javascript is also disabled

The template can be altered as desired, provided you place `<<CANONICAL_TO>>` wherever the target
address is needed. 

Configuration file, `redirect.config` specifies the local root of the local development copy of
the website on the first non-comment line.  Comments are indicated by #.  After the line containing the
complete path to the local root of the site, this file should contain one line for each desired 
redirect.  Each of these lines should include a pair separated by space or tabs.  First item of pair is
the location within the site (relative to root) to redirect from.  This can be a relative path to a file
or to a directory.  To indicate a directory, end with a trailing / otherwise a file will be assumed.
If a directory, the redirect file will be an index.html in that directory, otherwise it will be a file
with the exact name you provide including extension.  The second element of the pair is the full
canonical url of the new location, including either http or https as relevant.  See the provided `redirect.config`
for an example.
 
Usage:
 
- `java -jar JRedirectGenerator.jar -help`
   
   Outputs the usage instructions (this output).
	 
- `java -jar JRedirectGenerator.jar`
   
   Generates redirect pages using configuration from redirect.config and the page template from redirect.template.
	 
- `java -jar JRedirectGenerator.jar file.config`
     
   Allows specifying the configuration file that contains the redirects.
	 
- `java -jar JRedirectGenerator.jar file1.config file2.template`
   
   Allows specifying both configuration and template files, in that order.
