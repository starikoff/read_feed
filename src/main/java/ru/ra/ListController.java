package ru.ra;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

@Controller
public class ListController {

    @Autowired
    private FeedService feedService;

    @GetMapping("/")
    public String list(Model model) {
        List<FeedInfo> feeds = feedService.getFeedInfos();
        model.addAttribute("feeds", feeds);
        return "feeds";
    }

    @GetMapping("/posts")
    public String posts(@RequestParam("feed") Long feedId, Model model) {

        FeedLink feed = feedService.getFeed(feedId);
        model.addAttribute("feed", feed);

        Iterable<Post> posts = feedService.getUnreadPosts(feedId);
        model.addAttribute("posts", posts);

        return "posts";
    }

    @GetMapping("/add")
    public ModelAndView addFeed(
            @RequestParam(value = "url", required = false) String url,
            @RequestParam(value = "title", required = false) String title
    ) {
        if (StringUtils.isEmpty(url) || StringUtils.isEmpty(title)) {
            return new ModelAndView("feed_add");
        }
        feedService.addFeed(new FeedLink(url, title));
        return new ModelAndView(new RedirectView("/", true));
    }

    @GetMapping("/read")
    public ModelAndView readFeed(
            @RequestParam("feed") Long feedId
    ) {
        feedService.readFeed(feedId);
        return new ModelAndView(new RedirectView("/", true));
    }

    @GetMapping("/delete")
    public ModelAndView deleteFeed(@RequestParam("feed") Long feedId) {
        feedService.deleteFeed(feedId);
        return new ModelAndView(new RedirectView("/", true));
    }
}
