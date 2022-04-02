package im.vector.app.yiqia.department;

import java.util.List;

import im.vector.app.yiqia.BasePresenter;
import im.vector.app.yiqia.department.data.IDisplayBean;
import im.vector.app.yiqia.ui.BaseView;

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
