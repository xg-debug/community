package com.nowcoder.community.service;

import com.nowcoder.community.dao.elasticsearch.DiscussPostRepository;
import com.nowcoder.community.entity.DiscussPost;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ElasticsearchService {
    @Autowired
    private DiscussPostRepository discussRepository;

    @Autowired
    private ElasticsearchRestTemplate restTemplate;

    // 保存帖子
    public void saveDiscussPost(DiscussPost post) {
        discussRepository.save(post);
    }

    // 删除帖子
    public void deleteDiscussPost(int id) {
        discussRepository.deleteById(id);
    }

    // 根据关键字搜索帖子
    public List<DiscussPost> searchDiscussPost(String keyword) {
        // withQuery()用于构造搜索条件
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keyword, "title", "content"))
                .withSorts(SortBuilders.fieldSort("type").order(SortOrder.DESC),
                        SortBuilders.fieldSort("score").order(SortOrder.DESC),
                        SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0, 10))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();
        //查询
        SearchHits<DiscussPost> search = restTemplate.search(searchQuery, DiscussPost.class);
        //long count = restTemplate.count(searchQuery, DiscussPost.class);

        //得到查询返回的内容
        List<SearchHit<DiscussPost>> searchHits = search.getSearchHits();
        // 设置一个最后需要返回的实体类集合
        List<DiscussPost> discussPosts = new ArrayList<>();
        // 遍历返回的内容，进行处理
        for (SearchHit<DiscussPost> hit : searchHits) {
            // 获取高亮的内容
            Map<String, List<String>> highlightFields = hit.getHighlightFields();
            // 将高亮的内容添加到content中(匹配到的如果是多段，就将第一段高亮显示)
            // 没有匹配到关键字就显示原来的title和content
            hit.getContent().setTitle(highlightFields.get("title")==null ? hit.getContent().getTitle():highlightFields.get("title").get(0));
            hit.getContent().setContent(highlightFields.get("content")==null ? hit.getContent().getContent():highlightFields.get("content").get(0));
            // 放到实体类中
            discussPosts.add(hit.getContent());
        }
        return discussPosts;
    }

    // 根据关键字搜索帖子：帖子的数量
    public long searchDiscussPostCount(String keyword) {
        // withQuery()用于构造搜索条件
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery(keyword, "title", "content"))
                .withSorts(SortBuilders.fieldSort("type").order(SortOrder.DESC),
                        SortBuilders.fieldSort("score").order(SortOrder.DESC),
                        SortBuilders.fieldSort("createTime").order(SortOrder.DESC))
                .withPageable(PageRequest.of(0, 10))
                .withHighlightFields(
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();
        //查询
        long count = restTemplate.count(searchQuery, DiscussPost.class);
        return count;
    }
}
