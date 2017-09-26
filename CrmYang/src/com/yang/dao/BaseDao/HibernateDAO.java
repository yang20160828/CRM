package com.yang.dao.BaseDao;
import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.ss.formula.functions.T;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import com.yang.util.HibernatePage;
@SuppressWarnings({"all"})
public abstract class HibernateDAO<T> extends HibernateDaoSupport {
    protected final Log logger = LogFactory.getLog(getClass());
    
    /**
     * 返回此Dao所管理的Entity类型
     */
    protected Class<T> poClass;

    public HibernateDAO() {
        poClass = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];

    }
    public T getById(Serializable id) {
        return (T) getHibernateTemplate().get(poClass, id);
    }

    public void delete(T po) {
        getHibernateTemplate().delete(po);
    }

    public void deleteAll(List list){
        getHibernateTemplate().deleteAll(list);
    }

    public void deleteById(Serializable id) {
        Object o = getById(id);
        if (o != null) {
            getHibernateTemplate().delete(o);
        }
    }
    public List<T> findAll() {
        return getHibernateTemplate().find("from " + poClass.getName() + " obj");
    }

    public List<T> findByCriterion(final Criterion... criterion) {
        return getHibernateTemplate().executeFind(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                Criteria criteria = session.createCriteria(poClass);
                for (Criterion c : criterion) {
                    criteria.add(c);
                }
                return criteria.list();
            }
        });
    }

    public void save(T po) {
        getHibernateTemplate().merge(po);
    }
    public void saveorUpdate(T po) {
        getHibernateTemplate().saveOrUpdate(po);
    }

    public void create(T po) {
        getHibernateTemplate().save(po);
    }
    /**
     * 批量更新
     * @param polist
     */
    public void createAll(List polist) {
        getHibernateTemplate().saveOrUpdateAll(polist);
    }
    

    public void update(T po) {
        getHibernateTemplate().update(po);
    }

    public HibernatePage<T> doQuery(String hql, boolean cacheable, int curPage,
			int pageSize, Object... values) {
		return new HibernatePage<T>(hql, getSessionFactory()
				.getCurrentSession(), cacheable, curPage, pageSize, values);
		
	}
    public T doQueryUnique(final String hql, final Object... values) {
		return (T) this.getHibernateTemplate().execute(new HibernateCallback() {
			public Object doInHibernate(Session session)
					throws HibernateException, SQLException {
				Query query = session.createQuery(hql);
				query.setCacheable(true);
				for (int i = 0; i < values.length; i++) {
					query.setParameter(i, values[i]);
				}
				return query.uniqueResult();
			}
		});

	}

    public T doQueryFirst(final String hql, final Object... values) {
        return (T) this.getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query query = session.createQuery(hql);
                query.setMaxResults(1);
                query.setCacheable(true);
                for (int i = 0; i < values.length; i++) {
                    query.setParameter(i, values[i]);
                }
                List list = query.list();
                if (list.size() > 0) {
                    return (T) list.get(0);
                } else {
                    return null;
                }
            }
        });
    }


    public void executeHsql(final String hql, final Object... values) {
        this.getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query query = session.createQuery(hql);
                for (int i = 0; i < values.length; i++) {
                    query.setParameter(i, values[i]);
                }
                query.executeUpdate();
                return null;
            }
        }
        );
    }
    public List<Object[]> doSomeQueryList(final String sql, final boolean cacheable,final Object... values) {
        return doSomeQueryList(sql, cacheable, null, values);
    }
    public List<Object[]> doSomeQueryList(final String sql, final boolean cacheable, final Map<String,List> map,final Object... values) {
        return (List<Object[]>) this.getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query query = session.createQuery(sql);
                query.setCacheable(cacheable);
                if(values!=null){
                	for (int i = 0; i < values.length; i++) {
                		query.setParameter(i, values[i]);
                	}
                }
                if(map!=null){
                	for(String key:map.keySet()){
                		query.setParameterList(key, map.get(key));
                	}
                }
                return query.list();
            }
        });
    }
    public List<T> doQueryList(final String hql, final boolean cacheable, final Object... values) {
        return (List<T>) this.getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query query = session.createQuery(hql);
                query.setCacheable(cacheable);
                if(values!=null)
                for (int i = 0; i < values.length; i++) {
                    query.setParameter(i, values[i]);
                }
                return query.list();
            }
        });
    }
    /**
     * 查询
     *
     * @param criteria
     * @return
     */
    public List<T> findByCriteria(DetachedCriteria criteria) {
        return (List<T>) this.getHibernateTemplate().findByCriteria(criteria);
    }
    /**
     * 通过SQL查询
     *
     * @param sql
     * @return
     */
    public List queryByCriteriaSQL(final String sql, Class cls) {

        Criteria criteria = this.getHibernateTemplate().getSessionFactory().getCurrentSession().createCriteria(cls);
        criteria.add(Restrictions.sqlRestriction(sql));
        return criteria.list();
    }

    public List queryBySQL(final String sql, final List parameters) {
        List list = (List) this.getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException {
                Query query = session.createSQLQuery(sql);

                if (parameters != null && parameters.size() > 0) {
                    for (int i = 0; i < parameters.size(); i++) {
                        query.setParameter(i, parameters.get(i));
                    }
                }
                return query.list();
            }
        });

        return list;
    }
    
    public String getFromHql() {
        return "from " + poClass;
    }

    public long doQueryCount(final String hql, final Object... values) {
        return (Long) this.getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query query = session.createQuery(hql);
                query.setCacheable(true);
                if(values!=null)
                for (int i = 0; i < values.length; i++) {
                    query.setParameter(i, values[i]);
                }
                Long count = (Long) query.uniqueResult();
                if (count == null) {
                    return Long.parseLong("0");
                } else {
                    return count;
                }
            }
        });
    }

    public double doQuerySum(final String hql, final Object... values) {
        return (Double) this.getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query query = session.createQuery(hql);
                query.setCacheable(true);
                for (int i = 0; i < values.length; i++) {
                    query.setParameter(i, values[i]);
                }
                Double count = (Double) query.uniqueResult();
                if (count == null) {
                    return Double.parseDouble("0.0");
                } else {
                    return count;
                }
            }
        });
    }
    
    public List<T> doQueryLimitList(
            final String hql,
            final boolean cacheable,
            final int dataNum,
            final Object... values) {
        return (List<T>) this.getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query query = session.createQuery(hql);
                query.setCacheable(cacheable);
                if(values!=null)
                for (int i = 0; i < values.length; i++) {
                    query.setParameter(i, values[i]);
                }
                query.setFirstResult(0);
                query.setMaxResults(dataNum);
                return query.list();
            }
        });
    }
 
    public List<T> doQueryLimitList(
            final String hql,
            final boolean cacheable,
            final Map<String,List> map,
            final int dataNum,
            final Object... values) {
        return (List<T>) this.getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query query = session.createQuery(hql);
                query.setCacheable(cacheable);
                for (int i = 0; i < values.length; i++) {
                    query.setParameter(i, values[i]);
                }
                for(String key:map.keySet()){
                	query.setParameterList(key, map.get(key));
                }
                query.setFirstResult(0);
                query.setMaxResults(dataNum);
                return query.list();
            }
        });
    }
    
    public List<Object[]> doSQLQueryList(final String sql, final boolean cacheable, final Object... values) {
        return doSQLQueryList(sql, cacheable, null,values);
    }
    
    public List<Object[]> doSQLQueryList(final String sql, final boolean cacheable, final Map<String,List> map,final Object... values) {
        return (List<Object[]>) this.getHibernateTemplate().execute(new HibernateCallback() {
            public Object doInHibernate(Session session) throws HibernateException, SQLException {
                Query query = session.createSQLQuery(sql);
                query.setCacheable(cacheable);
                if(values!=null){
                	for (int i = 0; i < values.length; i++) {
                		query.setParameter(i, values[i]);
                	}
                }
                if(map!=null){
                	for(String key:map.keySet()){
                		query.setParameterList(key, map.get(key));
                	}
                }
                return query.list();
            }
        });
    }
}
