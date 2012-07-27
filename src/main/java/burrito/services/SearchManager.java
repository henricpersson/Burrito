/**
 * Copyright 2011 Henric Persson (henric.persson@gmail.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package burrito.services;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import siena.Model;
import burrito.annotations.BBCode;
import burrito.annotations.Displayable;
import burrito.annotations.RichText;
import burrito.annotations.SearchableField;
import burrito.annotations.SearchableMethod;
import burrito.client.util.StringCutter;
import burrito.client.widgets.panels.table.ItemCollection;
import burrito.client.widgets.panels.table.PageMetaData;
import burrito.util.EntityUtil;
import burrito.util.StringUtils;

import com.google.appengine.api.search.Consistency;
import com.google.appengine.api.search.Document;
import com.google.appengine.api.search.Index;
import com.google.appengine.api.search.IndexSpec;
import com.google.appengine.api.search.ListRequest;
import com.google.appengine.api.search.ListResponse;
import com.google.appengine.api.search.Query;
import com.google.appengine.api.search.QueryOptions;
import com.google.appengine.api.search.Results;
import com.google.appengine.api.search.ScoredDocument;
import com.google.appengine.api.search.SearchException;
import com.google.appengine.api.search.SearchServiceFactory;
import com.google.appengine.api.search.SortExpression;
import com.google.appengine.api.search.SortExpression.Builder;
import com.google.appengine.api.search.SortOptions;


/**
 * Idéerna till den här klassen kommer främst från
 * <nobr>http://googleappengine.blogspot
 * .com/2010/04/making-your-app-searchable-using-self.html</nobr>
 * 
 * Teknikerna för säkning går ut på att splitta texten i tokens som sen sparas i
 * en entitet i databasen. App engines datastore sköter indexeringen och man kan
 * ställa enkla queries.
 * 
 * @author henper
 * 
 */
public class SearchManager {

	private static final String CONTENT_FIELD_NAME = "content_"; //ending with an underscore to prevent clashes with true field names.
	private static final String TITLE_FIELD_NAME = "title_"; //ending with an underscore to prevent clashes with true field names.
	static SearchManager instance;

	public static SearchManager get() {
		if (instance == null) {
			instance = new SearchManager();
		}
		return instance;
	}

	
	private SearchManager() {
		//private
	}
	
	private Index getIndex() {
	    IndexSpec indexSpec = IndexSpec.newBuilder()
	        .setName("global")
	        .setConsistency(Consistency.PER_DOCUMENT)
	        .build();
	    return SearchServiceFactory.getSearchService().getIndex(indexSpec);
	}
	
	public void insertOrUpdateSearchEntry(Model entity, Long entityId) {
		Class<? extends Model> ownerType = entity.getClass();
		
		Document document = entityToDocument(ownerType, entity, entityId);
		getIndex().add(document);
		
	}

	public void deleteSearchEntry(Class<? extends Model> ownerType, Long entityId) {
		getIndex().remove(ownerType.getName() + ":" + entityId);
	}

	private Document entityToDocument(Class<? extends Model> ownerType, Model entity, Long entityId) {
		Document.Builder builder = Document.newBuilder()
			.setId(ownerType.getName() + ":" + entityId)
			.addField(com.google.appengine.api.search.Field.newBuilder().setName("ownerType").setText(ownerType.getName()))
			.addField(com.google.appengine.api.search.Field.newBuilder().setName(TITLE_FIELD_NAME).setText(entity.toString()));;
			
		Set<String> searchables = new HashSet<String>();
		
		for (Field field : EntityUtil.getFields(ownerType)) {
			if (field.isAnnotationPresent(SearchableField.class) || field.isAnnotationPresent(Displayable.class)) {
				try {
					field.setAccessible(true);
					Object obj = field.get(entity);
					String val = null;
					if (obj != null) {
						val = obj.toString();
						if (field.isAnnotationPresent(RichText.class)) {
							val = StringUtils.stripHTML(val);
						}
						if (field.isAnnotationPresent(BBCode.class)) {
							val = StringUtils.stripBBCode(val);
						}
						searchables.add(val);
					}
					if (field.isAnnotationPresent(Displayable.class)) {
						builder.addField(com.google.appengine.api.search.Field.newBuilder().setName(field.getName()).setText((val == null)? "" : val));
					}
				} catch (Exception e) {
					throw new RuntimeException("Failed to get searchable texts from entity", e);
				}
			}
		}
		
		for (Method method : EntityUtil.getMethods(ownerType)) {
			if (method.isAnnotationPresent(SearchableMethod.class) || method.isAnnotationPresent(Displayable.class)) {
				try {
					method.setAccessible(true);
					Object obj = method.invoke(entity);
					if (obj != null) {
						searchables.add(obj.toString());
					}
				} catch (Exception e) {
					throw new RuntimeException("Failed to get searchable texts from entity", e);
				}
			}
		}  
		
		String text = concatenate(searchables);
		builder.addField(com.google.appengine.api.search.Field.newBuilder().setName(CONTENT_FIELD_NAME).setText(text));
		return builder.build();
	}


	private String concatenate(Set<String> texts) {
		StringBuilder sb = new StringBuilder();
		for (String text : texts) {
			if (sb.length() > 0) {
				sb.append(". ");
			}
			sb.append(text);
		}
		return sb.toString();
	}

	public ItemCollection<SearchEntry> search(Class<? extends Model> clazz, String query) {
		return search(clazz, query, new PageMetaData<String>(100, 0, null, true));
	}
	

	/**
	 * Makes a global search, i.e. not filtering on a specific entity kind.
	 * 
	 * @param query
	 */
	public ItemCollection<SearchEntry> search(String query) {
		return search(query, new PageMetaData<String>(100, 0, null, true));
	}
	
	/**
	 * Makes a global search, i.e. not filtering on a specific entity kind.
	 * 
	 * @param query the search query
	 * @param pageMetaData The current page
	 * @return
	 */
	public ItemCollection<SearchEntry> search(String query, PageMetaData<String> pageMetaData) {
		return search(null, query, pageMetaData);
	}


	/**
	 * Searches for entries matching a query.
	 * 
	 * @param clazz
	 * @param query
	 * @param p
	 * @return
	 */
	public ItemCollection<SearchEntry> search(Class<? extends Model> clazz, String query, PageMetaData<String> page) {
		
		query = escape(query);
		try {
			QueryOptions.Builder options = QueryOptions.newBuilder()
				.setFieldsToSnippet(CONTENT_FIELD_NAME)
				.setFieldsToReturn(CONTENT_FIELD_NAME, TITLE_FIELD_NAME)
				.setLimit(page.getItemsPerPage())
				.setOffset((int) page.getRangeStart());
			
			
			if (page.getSortKey() != null) {
				Builder sortExpression = SortExpression.newBuilder();
				sortExpression.setExpression(page.getSortKey());
				sortExpression.setDirection((page.isAscending()) ? SortExpression.SortDirection.ASCENDING : SortExpression.SortDirection.DESCENDING);
				sortExpression.setDefaultValue("");
				SortOptions.Builder sortOptions = SortOptions.newBuilder()
					.setLimit(1000)
					.addSortExpression(sortExpression);
				options.setSortOptions(sortOptions);
			}
			String ownerTypeFilter = "";
			if (clazz != null) {
				//Filter on entity kind
				ownerTypeFilter = "ownerType:\""+clazz.getName()+"\" ";
			}
			Query q = Query.newBuilder().setOptions(options).build(ownerTypeFilter + CONTENT_FIELD_NAME+":\"" + query + "\"");

		    Results<ScoredDocument> results = getIndex().search(q);
		    List<SearchEntry> entries = new ArrayList<SearchEntry>();
		    for (ScoredDocument document : results) {
		    	SearchEntry entry = documentToSearchEntry(document, query);
		        entries.add(entry);
		    }
		    ItemCollection<SearchEntry> result = new ItemCollection<SearchEntry>();
		    result.setHasNextPage(false);
		    result.setItems(entries);
		    result.setHasNextPage(entries.size() == page.getItemsPerPage());
		    result.setItemsPerPage(page.getItemsPerPage());
		    return result;
		    
		} catch (SearchException e) {
			throw new RuntimeException("Failed search", e);
		}
	}

	private String escape(String query) {
		if (query == null) {
			return null;
		}
		return query.replace("\"", "");
	}


	private SearchEntry documentToSearchEntry(Document document, String query) {
		String docId = document.getId();
		String[] splitId = docId.split(":");
		
		
		SearchEntry entry = new SearchEntry();
		entry.setOwnerClass(splitId[0]);
		entry.setOwnerId(Long.valueOf(splitId[1]));
		
		com.google.appengine.api.search.Field content = document.getOnlyField(CONTENT_FIELD_NAME);
		String snippet = content.getHTML();
		if (snippet == null && query != null) {
			//In devmode, snippets are not created. Therefor, we add our own. 
			//See http://code.google.com/p/googleappengine/issues/detail?id=7473
			snippet = createSnippet(content.getText(), query);
		}
		
		entry.setSnippetHtml(snippet);
		entry.setTitle(document.getOnlyField(TITLE_FIELD_NAME).getText());
		return entry;
	}
	
	
	private String createSnippet(String test, String query) {
		int nameIndex = test.toLowerCase().indexOf(query);
		if (nameIndex != -1) {
			String highlight = test.substring(nameIndex, nameIndex
					+ query.length());
			StringBuffer sb = new StringBuffer(new StringCutter(100).cut(test, true,
					nameIndex));
			// update name index from the cut string
			nameIndex = sb.toString().toLowerCase().indexOf(query);
			if (nameIndex == -1) {
				//something wrong
				return sb.toString();
			}
			sb.replace(nameIndex, nameIndex + query.length(),
					"<span class=\"snippet-highlight\">" + highlight
							+ "</span>");
			return sb.toString();
		}
		return null;
	}
	


	public List<SearchEntry> getAllEntries() {
		List<SearchEntry> result = new ArrayList<SearchEntry>();
		ListResponse<Document> response = getIndex().listDocuments(ListRequest.newBuilder().build());
		for (Document doc : response) {
            result.add(documentToSearchEntry(doc, null));
        }
		return result;
	}


	public void clearIndexForEntity(Class<? extends Model> clazz) {
		while (true) {
			List<String> docIds = new ArrayList<String>();
			// Return a set of document IDs.
			QueryOptions.Builder options = QueryOptions.newBuilder()
					.setLimit(100).setReturningIdsOnly(true);

			Query q = Query.newBuilder().setOptions(options)
					.build("ownerType:" + clazz.getName());

			Results<ScoredDocument> results = getIndex().search(q);
			if (results.getNumberFound() == 0) {
				break;
			}
			for (ScoredDocument document : results) {
				docIds.add(document.getId());
			}
			getIndex().remove(docIds);
	    }
	}


	
}
