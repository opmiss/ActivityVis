from afinn import sentiment
from dateutil import parser
from ttp import ttp
from twitter import *
from whoosh.collectors import Collector, FilterCollector, WrappingCollector
from whoosh.fields import TEXT, Schema, ID, STORED, DATETIME
from whoosh.index import create_in, open_dir
from whoosh.qparser import MultifieldParser, QueryParser
from whoosh.qparser.dateparse import DateParserPlugin
from whoosh.sorting import MultiFacet, ScoreFacet
import math
import simplejson as json
import time

class fetcher(object):
    
    def __init__(self, path):
        self.idxpath = path
        self.ix = open_dir(self.idxpath)
        self.query = MultifieldParser(['content','ctime'], schema=self.ix.schema)
        self.query.add_plugin(DateParserPlugin())
        self.sorter = MultiFacet(["ctime", ScoreFacet()])
        self.parser = ttp.Parser();
        self.dateparser = parser.parser();
        
    def fetch_thread_by_tid(self, retid):
        t1 = int(round(time.time() * 1000))
        tweets = []
        try :
            searcher = self.ix.searcher()
            results = searcher.documents(retweetid=retid)
            for r in results:
                tweet = json.loads(r['json'])
                tweet['created_at'] = self.dateparser.parse(tweet['created_at'])
                tweets.append(tweet)
        except Exception as e:
            print 'fetch_tweets error' + str(e)
        finally:
            searcher.close()
        t2 = int(round(time.time() * 1000))
        tweets = sorted(tweets, key=lambda x: x['created_at'], reverse=False)
        print '----> fetch tweets by retweet id ' + str(t2 - t1) + ' ms'
        return tweets
     
    def fetch_tweets_by_uid(self, uid):
        t1 = int(round(time.time() * 1000))
        try :
            searcher = self.ix.searcher()
            results = searcher.documents(ownerid=uid)
            tweets = []
            for r in results:
                tweet = json.loads(r['json'])
                tweet['user']['retweet_at'] = self.dateparser.parse(tweet['created_at'])
                tweet['created_at'] = self.dateparser.parse(tweet['created_at'])
                tweets.append(tweet)
        except Exception as e:
            print 'fetch_tweets error' + str(e)
        finally:
            searcher.close()
        t2 = int(round(time.time() * 1000))
        print '----> fetch tweets for the specified user costs ' + str(t2 - t1) + ' ms'
        return tweets
    
    def fetch_tweets_by_keyword(self, keyword, start, topk):
        
        print 'thread : '  + keyword
        
        t1 = int(round(time.time() * 1000))
        tweets = []
        users = []
        tweetids = {}
        qtext = unicode('ctime:[' + str(start) + ' to] AND ' + 'content:(' + keyword + ')')
        try :
            searcher = self.ix.searcher()
            q = self.query.parse(qtext)
            results = searcher.search(q)
            
            for r in results:
                t = json.loads(r['json'])
                tt = t;
                if 'retweeted_status' in t and t['retweeted_status'] is not None:
                    t = t['retweeted_status']
                tid = t['id_str']
                if tid not in tweetids:
                    
                    user = {
                        "id":tt['user']['id_str'], 
                        "retweet_time":self.dateparser.parse(tt['created_at']).strftime('%Y%m%d%H%M%S'),
                        "screen_name":tt['user']['screen_name'], 
                        "profile_image_url":tt['user']['profile_image_url'],
                        "followers_count":tt['user']['followers_count']
                    };
                    
                    users.append(user)
                    
                    tweet = {}
                    tweet['id'] = tid
                    tweet['text'] = t['text']
                    tweet['creator'] = {}
                    tweet['creator']['id'] = t['user']['id_str']
                    tweet['creator']['creator'] = t['user']['screen_name']
                    tweet['creator']['creator_img'] = t['user']['profile_image_url']
                    tweet['retweet_count'] = t['retweet_count']
                    tweet['created_at'] = self.dateparser.parse(t['created_at']).strftime('%Y%m%d%H%M%S')
                    tweet['retweet_history'] = [user]
                    tweet['rank'] = max(t['user']['followers_count'], tt['user']['followers_count']) * t['retweet_count']
                    tweetids[tid] = tweet
                    tweets.append(tweet)
                else :
                    user = {
                        "id":tt['user']['id_str'], 
                        "retweet_time":self.dateparser.parse(tt['created_at']).strftime('%Y%m%d%H%M%S'),
                        "screen_name":tt['user']['screen_name'], 
                        "profile_image_url":tt['user']['profile_image_url'],
                        "followers_count":tt['user']['followers_count']
                    };
                    users.append(user)
                    tweetids[tid]['retweet_history'].append(user)
                    tweetids[tid]['rank'] = max(tweetids[tid]['rank'], tt['user']['followers_count'] * t['retweet_count'])
                    print '--> update retweet history'
            
            tweets = sorted(tweets, key=lambda x: x['rank'], reverse=False)[:topk]
            tweets = sorted(tweets, key=lambda x: self.dateparser.parse(x['created_at']), reverse=False)
            
        except Exception as e:
            print 'error ' + str(e)
        finally:
            searcher.close()
        t2 = int(round(time.time() * 1000))
        print '----> fetch tweets for the specified user costs ' + str(t2 - t1) + ' ms'
        return (tweets, users)
    
    def fetch_retweeting_behavior(self, uid):
        
        tweets = self.fetch_tweets_by_uid(uid)
        
        print tweets
        
        glyph = {}
        glyph['threads'] = []
        glyph['users'] = []
        
        temp = []
        thread_tweets = []
        for tweet in tweets:
            tid = tweet['id']
            if 'retweeted_status' in tweet and tweet['retweeted_status'] is not None:
                tid = tweet['retweeted_status']['id']
            
            thread_retweets = self.fetch_thread_by_tid(unicode(tid))
            if(len(thread_retweets) == 0) :
                continue
            temp.append(tweet)
            thread_tweets.append(thread_retweets)
        
        tweets = temp
        
        behaviors = {}
        for i in range(len(tweets)):
            tweet = tweets[i]
            tid = tweet['id']
            if 'retweeted_status' in tweet and tweet['retweeted_status'] is not None:
                tid = tweet['retweeted_status']['id']
            
            thread = {}
            thread['content'] = []
            for tt in thread_tweets[i]:
                u = tt['user']
                if u['id'] not in behaviors:
                    behaviors[u['id']] = {};
                    behaviors[u['id']]['id'] = u['id']
                    behaviors[u['id']]['screen_name'] = u['screen_name']
                    behaviors[u['id']]['followers_count'] = u['followers_count']
                    behaviors[u['id']]['profile_image_url'] = u['profile_image_url']
                    behaviors[u['id']]['behavior'] = [0] * len(tweets);
                    behaviors[u['id']]['time'] = [''] * len(tweets);
                    behaviors[u['id']]['sentiments'] = [0] * len(tweets);
                    behaviors[u['id']]['rank'] = u['followers_count']
                    glyph['users'].append(behaviors[u['id']])
                behaviors[u['id']]['behavior'][i] = 1
                behaviors[u['id']]['time'][i] = tt['created_at'].strftime('%Y%m%d%H%M%S')
                behaviors[u['id']]['sentiments'][i] = sentiment(tweet['text'])
                thread['content'].append(behaviors[u['id']])
                        
            thread['name'] = tweet['text']
            thread['sentiment'] = sentiment(tweet['text'])
            thread['start'] = thread_tweets[i][0]['created_at'].strftime('%Y%m%d%H%M%S')
            thread['end'] = thread_tweets[i][len(thread_tweets[i]) - 1]['created_at'].strftime('%Y%m%d%H%M%S')
            glyph['threads'].append(thread)
        
        for userid in behaviors:
            behaviors[userid]['sentiment'] = 1.0 * sum(behaviors[userid]['sentiments']) / len(behaviors[userid]['sentiments'])
            del behaviors[userid]['sentiments']
        
        glyph['start'] = glyph['threads'][0]['start']
        glyph['end'] = glyph['threads'][len(glyph['threads']) - 1]['start']
        json.dump(glyph, open('./' + str(uid) + '.retweet' + '.json', 'wb'))
        
        return glyph
    
    def fetch_topic_behavior(self, uid):
        tags = {}
        tweets = self.fetch_tweets_by_uid(uid)
        for tweet in tweets:
            res = self.parser.parse(tweet['text'])
            if(len(res.tags) == 0):
                continue
            
            for tag in res.tags:
                if tag not in tags:
                    tags[tag] = tweet['created_at']
                else :
                    if tags[tag] > tweet['created_at']:
                        tags[tag] = tweet['created_at']
        
        glyph = {}
        glyph['start'] = '201210040000'
        glyph['end'] = '201210040600'
        glyph['threads'] = []
        glyph['users'] = []
        # construct thread
        behaviors = {}
        tid = 0
        for tag in tags:
            thread = {}
            thread['name'] = tag
            thread['start'] = tags[tag].strftime('%Y%m%d%H%M%S')
            (tweets, users) = self.fetch_tweets_by_keyword('#' + tag, thread['start'], 300)
            
            thread['content'] = []
            for t in tweets:
                t['time'] = [''] * len(tags.keys())
                t['time'][tid] = t['created_at']
                thread['content'].append(t)
                
            thread['end'] = thread['content'][len(thread['content']) - 1]['created_at']
            print thread['end']
            glyph['threads'].append(thread)
            
            for u in users:
                if u['id'] not in behaviors:
                    behaviors[u['id']] = {};
                    behaviors[u['id']]['id'] = u['id']
                    behaviors[u['id']]['screen_name'] = u['screen_name']
                    behaviors[u['id']]['followers_count'] = u['followers_count']
                    behaviors[u['id']]['behavior'] = [0] * len(tags.keys());
                    glyph['users'].append(behaviors[u['id']])
                    
                behaviors[u['id']]['behavior'][tid] = 1
            tid += 1
        
        glyph['start'] = glyph['threads'][0]['start']
        glyph['end'] = glyph['threads'][len(glyph['threads']) - 1]['start']
            
        json.dump(glyph, open('./' + str(uid) + '.topic' + '.json', 'wb'))

if __name__ == '__main__':
    f = fetcher('../data/index/20121004/')
#    f.fetch_users('care', '201210040000', '201210040600')
#    f.fetch_topic_behavior(u'339467938')
    f.fetch_retweeting_behavior(u'339467938')