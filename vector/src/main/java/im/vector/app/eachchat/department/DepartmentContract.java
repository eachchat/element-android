package im.vector.app.eachchat.department;

import java.util.List;

import im.vector.app.eachchat.base.BasePresenter;
import im.vector.app.eachchat.department.data.IDisplayBean;
import im.vector.app.eachchat.ui.BaseView;

/**
 * Created by zhouguanjie on 2019/9/10.
 */
public interface DepartmentContract {

    interface View extends BaseView<Presenter> {

        void showData(List<IDisplayBean> departments, boolean isAllUser, int showMembersTagPos);

    }

    interface Presenter extends BasePresenter {

        void loadData(List<IDisplayBean> departments, boolean isAllUser, int showMembersTagPos);

        void initData(String departmentId);

    }
}
