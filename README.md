## Description

The app provides a mapping service allowing urls to be *prettified* based on the mappings present in internally defined datasource. An sample table is illustrated below:

|From:|To:|
|-|-|
| /products | /Fashion/ |
| /products?tag=5678| /Boat--Shoes/ |

***

## Folder structure
```
| src
│ ├─ main.java.com.stylight.url.prettier                   
|      ├─ controllers
|      		└─ UrlPrettierController.java			# Controller for the lookup and inverse lookup requests
|      ├─ services
|      		├─ interfaces
|	      		└─ UrlPrettierServiceInterface.java 	# Basic interface encapsulating the methods exposed by the corresponding service
|	      	└─ UrlPrettierService.java 		# Implementation of the interface encapsulating the business logic and exposing it to the controllers
|      ├─ datasource
|      		└─ UrlMappingsDatasource.java 		# Mock database class for retaining the mappings between pretty urls and their counterparts
|      ├─ models
|      		└─ RequestDTO.java 			# Object used to encapsulate requests
|			└─ ResponseDTO.java 			# Object used to encapsulate responses
│ ├─ test.java.com.stylight.url.prettier                   
|      ├─ services
|      		└─ UrlPrettierServiceTests.java			# Unit tests for the UrlPrettierService.
|      └─ UrlPrettierControllerIntegrationTests.java			# Integration tests for the UrlPrettierController.	
```

----
## Implementation Details

### Preliminary Steps
- The data is retained within the datasource class by using 4 Maps:
	1. ```routeToPrettyUri```: A bi-directional map retaining used to hold the direct mapping from a route to a pretty url (if applicable). Example:
		- |From:|To:|
		  |-|-|
		  | /products | /Fashion/ |
	2. ```queryParamsToPrettyUri```: A bi-directional map retaining from query params to the associated pretty url. Example:
		- |From:|To:|
		  |-|-|
		  | gender=female&tag=123&tag=1234 | /Women/Shoes/ |
	3. ```queryParamsToRoute```: A hash map retaining mappings from query params to the associated route. Example:
		- |From:|To:|
		  |-|-|
		  | gender=female&tag=123&tag=1234 | /products |
	4. ```routeToQueryParams```: A hash multimap retaining mappings from a route to all possible query param mappings. Example:
		- |From:|To:|
		  |-|-|
		  | /products | gender=female, gender=female&tag=123&tag=1234, brand=123, tag=5678 |

- The algorithm detailing the logic utilizing these maps is illustrated in the next section.

### Master branch implementation
```Java

# Maps a list of provided urls to their prettified counterparts

-------------------------
Algorithm 1: lookup(urls)
-------------------------
INPUT: List of urls
OUTPUT: List of prettified urls

FUNCTION lookup(urls):
  	matches = []
	FOR uri in urls DO
		pathSegment = getPathSegments(uri)
		accumulatedSegments = []
		FOR segment in pathSegment DO
			accumulatedSegments.add(segment)
			matchedUrl = routeToPrettyUri(accumulatedSegments)
		END
		querySegments = getQuerySegments(uri)

		IF querySegments is not null
		THEN
			routeLinkedQuerySegments = getRouteToQueryParams(pathSegments)
			longestRouteLinkedQuerySegment = getLongestMatchingLToRSubstring(routeLinkedQuerySegments)
			matchedUrl = queryParamsToPrettyUri(longestRouteLinkedQuerySegment)
			unmatched = getUnmatchSubstring(longestRouteLinkedQuerySegment)
			IF unmatched is not null 
			THEN 
				addUnmatchedQueryParamsToMatchedUrl(matchedUrl, unmatched)
			ENDIF
		ENDIF
		matches.add(matchedUrl)
	END
	return matches
END
```

```Java

# Maps a list of provided prettified urls to their counterparts

--------------------------------
Algorithm 2: reverseLookup(urls)
--------------------------------
INPUT: List of prettified urls
OUTPUT: List of corresponding urls

FUNCTION reverseLookup(urls):
  	matches = []
	FOR uri in urls DO
		pathSegment = getPathSegments(uri)
		
		# Try to match the complete url
		reverseUrl = extractReversedUrl(pathSegments);

		if reverseUrl is null
		THEN
			accumulator = []
			FOR segment in pathSegments DO
				accumulator.add(segment)
				reversedSegment = matchReversedUrl(accumulator)
				if reversedSegment is NULL
				THEN
					accumulator.remove(segment)
					reverseUrl.add(pathSegments.remove(pathSegments))
				ENDIF
			END
		ENDIF

		IF reverseURL is not NULL
		THEN
			# Add query segments that have been erroneously added included in the pretty url to the reversed one as well.
			reverseUrl.add(getQuerySegments(uri))
		ENDIF
		matches.add(reverseUrl)
	END
	return matches
END

FUNCTION matchReversedUrl(pathSegments):
	reverseUrl = routeToPrettyUri(pathSegments)

	if reverseUrl is null 
	THEN
		queryParams = queryParamsToPrettyUrl.Inverse.Get(pathSegments)
		if queryParams is not NULL
		THEN
			reverseUrl = getQueryParamsToRoute(queryPrarams)
		ENDIF
	ENDIF
	return reverseUrl
END


```
***

### Lookup
-  A  ```RequestDTO.java``` object encapsulating the list of urls that the user wishes to prettify is sent via **POST** to the *lookup* endpoint.
-  The ```RequestDTO.java``` object arrives at the ```lookup``` method of the ```UrlPrettierController```.
-  The controller method invokes the ```lookup``` method of the  ```UrlPrettierService```.
-  The ```UrlPrettierService``` *prettifies* the list of input urls by doing the following:
	1. Checks the route params of the input url first to check if the segments match with a prettified reprsentation, storing a partial match if present
		- Subsequently if query params are present, the ``routeToQueryParams`` datasource is checked to get the list of query parameters associated with this route.
			- From this list, the longest matching substring of query params(left to right) is obtained and used to index into the ``queryParamsToPrettyUri`` to get the corresponding pretty url.
			- Any unmatched segments are concatenated to this pretty url (if present) and then added to the list of prettified urls.
	2. If not match is found, the input url is added into the list of matched urls.
	3. The list of prettified urls is returned to the controller, which wraps it in a ```RequestDTO.java``` object and returns it.
### Reverse Lookup
-  A  ```RequestDTO.class``` object encapsulating all the inverse mapping a user desires is sent via **POST** to the *reverseLookup* endpoint.
-  The contained url arrives at the ```reverseLookup``` method of the  ```UrlPrettierController```.
-  The controller method invokes the ```reverseLookup``` method of the  ```UrlPrettierService```.
-  The ```UrlPrettierService``` *decodes* this list of input urls by doing the following:
	1. Checks if the complete input url has a correponding decoded url counterpart
		- If exists, append to matches list
	2. Otherwise, incrementally check increasingly larger segments of the url until the datasource returns a null. Append the url corresponding to the maximally matched segment (also append the unmatched route/query segments to this match) to the list of matches.
	3. Append the input url if no decoded url counterpart exists.

#### Pros
1. **Scalability**: The map implementation while complex due to the large number of maps involved, allows the implementation to scale better due to the splitting of the url into query and route segments
2. **Performance**:
	- Let the number of urls be **n**
	- The sum of path segments and query params equal **m**
	- For finding a pretty url, the alogrithm tries to find a maximally matching sequence which by doing a lookup in a dictionary (${O(1)}$).
	- Examing the worst case would be looking at a route with a maximally matching query sequence. For example:
		- |From:|To:|
	  	  |-|-|
		  |/products|/Fashion/|
	  	  |/products?tag=123&tag=12345|/Adidas/|
		- The input url would be ```/products?tag=123&tag=12345```. In this case, the route params are first checked for a match. ```/products/``` is matched to ```/Fashion/```. This ${O(1)}$ operation has to be done for incrementing sequences of route params for a match (i.e. num(route params) * ${O(1)}$). 
		- For query params, we check do an ${O(1)}$ operation to find the list of query params mapped to the route param. 
		- This is followed by an iteration over the list to get the maximum matching sequence of query params which yields ${O(1) * sizeof(list)}$ which can be safely assumed to be ${O(1)}$. 
		- The last operation is a concatenation of the matched url with the unmatched sequence, also an ${O(1)}$ operation.
		- In the worst case therefore, we have to iterate over all **m** parameters giving us a worst case complexity of ${O(nm)}$
		- It is safe to assume that ${m \ll n}$ so the overall complexity can be assumed to be ${O(n)}$
	- The algorithm therefore scales linearly with the number of input urls.

#### Cons
1. Prettified urls are assumed to be devoid of query params and any included query params are purposefully ignored for reverse lookups.
2. It is assumed that the tags linked to a route are unique. For instance, for the route of the form ```/products?tag=123```, ```tag=123``` has a mapping to the route ```/products``` thus ```tag=123``` cannot be associated with any other url. However ```tag=123&tag=1234``` being linked to another route is allowed.
#### To Note
1. As part of the implementation requirements, the order of query params is **IMPORTANT**. ```/product?tag=123&tag=1234``` is **not** treated equivalently to ```/product?tag=1234&tag=123```. 
	- The solution to this is trivial i.e. sorting the tags before storing them in the datasource and pre-sorting the params before using them for lookup operations.
2. Tags are matched as strings allowing and partial matches for tags are allowed. For example, given the following mapping:
	- |From:|To:|
	  |-|-|
	  | /products?brand=123| /Adidas/ |
	if the sent query is of the form ```/product?tag=1234``` the algorithm will mark this as a partial match and return: ```/Adidas/?4```.

- - ----
### Alternative approaches

1. A singular Bidirectional map can be used to contain all the mappings like below:
	- 	|From:|To:|
		|-|-|
		| /products?brand=123| /Adidas/ |
		| /products| /Fashion/ |
		| /products?tag=5678 |/Boat--Shoes/|
	- Lookups just involved breaking the url into route and query params and incrementally joining the route params and subsequently query params while doing a lookup in the dictionary at each step to find a match (at the same time retaining the maximally matched sequence). The complexity in this case would always be ${O(nm)}$ which is comparable to the current approach.
	- Keeping a singular map however makes the approach inscalable since each route and query param combination will require an entry in the same map. The performance (while theoretically should remain ${O(1)}$), will degrade with the number of keys in the map.
2. Using a data structure that involved a hashmap for the routes that in turn holds a list of linked lists for each path segment/query param that can be directly attached to this route. Each element in this list will in turn be the same data structure and encapsulate connections to the route and query parameters that can be linked to it.
	- This data structure makes it trivial to create the associated pretty url by just traversing the linked list.
	- The size grows exponentially since each route/query param would be a data structure with a list of linked lists. 

## Usage

### Lookup
```bash

$ curl -X POST -H "Content-Type: application/json" -d '{"urls":["/products", "/products?gender=female", "/products?gender=female&tag=123&tag=1234", "/products?gender=female&tag=123&tag=1234&tag=5678&brand=123", "/products?brand=123", "/products?brand=1234", "/lookup?brand=123", "/lookups", "/products?tag=5678"]}' http://localhost:8081/lookup

```
    
with a data file

```bash
$ curl -d "@data.json" -X POST http://localhost:8081/lookup
```

#### Sample Response
```JSON
{
	"urls": [
		"/Fashion/",
		"/Women/",
		"/Women/Shoes/",
		"/Women/Shoes/?tag=5678&brand=123",
		"/Adidas/",
		"/Adidas/?4",
		"/lookup?brand=123",
		"/lookups",
		"/Boat--Shoes/"
	]
}
```

### Reverse Lookup
```bash

$ curl -X POST -H "Content-Type: application/json" -d '{"urls":["/Fashion/","/Women/","/Women/Shoes/","/lookups","/Boat--Shoes/"]}' http://localhost:8081/reverseLookup
```
    
with a data file

```bash
$ curl -d "@data.json" -X POST http://localhost:8081/reverseLookup
```

#### Sample Response
```JSON
{
	"urls":[
		"/products",
		"/products?gender=female",
		"/products?gender=female&tag=123&tag=1234",
		"/lookups",
		"/products?tag=5678"
	]
}
```
- - -
## Running the app

### Docker
```bash
$ cd prettier
# Build the image
$ docker-compose build
# Run the application
$ docker-compose up -d
```

\* The application is hosted at ```http://127.0.0.1:8081```
