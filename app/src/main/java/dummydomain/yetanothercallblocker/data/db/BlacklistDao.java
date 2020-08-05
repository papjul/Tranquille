package dummydomain.yetanothercallblocker.data.db;

import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.SqlUtils;
import org.greenrobot.greendao.query.CloseableListIterator;
import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

public class BlacklistDao {

    public interface DaoSessionProvider {
        DaoSession getDaoSession();
    }

    private static final Logger LOG = LoggerFactory.getLogger(BlacklistDao.class);

    private final DaoSessionProvider daoSessionProvider;

    public BlacklistDao(DaoSessionProvider daoSessionProvider) {
        this.daoSessionProvider = daoSessionProvider;
    }

    public List<BlacklistItem> loadAll() {
        return getBlacklistItemDao().queryBuilder()
                .orderAsc(BlacklistItemDao.Properties.Pattern)
                .list();
    }

    public <T extends Collection<BlacklistItem>> T detach(T items) {
        BlacklistItemDao dao = getBlacklistItemDao();
        for (BlacklistItem item : items) {
            dao.detach(item);
        }
        return items;
    }

    public BlacklistItem findById(long id) {
        return getBlacklistItemDao().load(id);
    }

    public void save(BlacklistItem blacklistItem) {
        getBlacklistItemDao().save(blacklistItem);
    }

    public void delete(Iterable<Long> keys) {
        getBlacklistItemDao().deleteByKeyInTx(keys);
    }

    public long countValid() {
        return getBlacklistItemDao().queryBuilder()
                .where(BlacklistItemDao.Properties.Invalid.notEq(true)).count();
    }

    public BlacklistItem getFirstMatch(String number) {
        try (CloseableListIterator<BlacklistItem> it = getMatchesQueryBuilder(number).build()
                .listIterator()) {
            if (it.hasNext()) return it.next();
        } catch (IOException e) {
            LOG.debug("getFirstMatch()", e);
        }
        return null;
    }

    private QueryBuilder<BlacklistItem> getMatchesQueryBuilder(String number) {
        return getBlacklistItemDao().queryBuilder()
                .where(BlacklistItemDao.Properties.Invalid.notEq(true),
                        new InverseLikeCondition(BlacklistItemDao.Properties.Pattern, number))
                .orderAsc(BlacklistItemDao.Properties.CreationDate);
    }

    private BlacklistItemDao getBlacklistItemDao() {
        return daoSessionProvider.getDaoSession().getBlacklistItemDao();
    }

    private static class InverseLikeCondition extends WhereCondition.PropertyCondition {
        InverseLikeCondition(Property property, String value) {
            super(property, " ? LIKE ", value);
        }

        @Override
        public void appendTo(StringBuilder builder, String tableAlias) {
            builder.append(op);
            SqlUtils.appendProperty(builder, tableAlias, property);
        }
    }

}