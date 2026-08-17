import moment from 'moment';
import { HelperService } from './util';

export class DateHelper {

  public static startOfMonth() {

    let format = 'YYYY-MM-DD';
    const startOfMonth = moment().startOf('month').format(format);

    return moment(startOfMonth, format).toDate();
  }

  public static endOfMonth() {

    let format = 'YYYY-MM-DD';
    const endOfMonth = moment().endOf('month').format(format);

    return moment(endOfMonth, format).toDate();
  }

  public static startOfYear() {
    const format = 'YYYY-MM-DD';
    return moment(moment().startOf('year').format(format), format).toDate();
  }

  public static endOfYear() {
    const format = 'YYYY-MM-DD';
    return moment(moment().endOf('year').format(format), format).toDate();
  }

    public static startOfWeek() {
        let format = 'YYYY-MM-DD';
        const startOfWeek = moment().startOf('week').format(format);

        return moment(startOfWeek, format).toDate();
    }

    public static endOfWeek() {
        let format = 'YYYY-MM-DD';
        const endOfWeek = moment().endOf('week').format(format);

        return moment(endOfWeek, format).toDate();
    }

    public static combineDateAndTime(date: Date, time: Date): Date {
        const combinedDateTime = new Date(date);

        combinedDateTime.setHours(time.getHours());
        combinedDateTime.setMinutes(time.getMinutes());
        combinedDateTime.setSeconds(time.getSeconds());
        combinedDateTime.setMilliseconds(time.getMilliseconds());

        return combinedDateTime;
    }

    public static matDateToSql(matDate: Date | string | null): string {
        if (!matDate) return '';
        const m = typeof matDate === 'string'
            ? moment(matDate, 'MMMM DD, YYYY')
            : moment(matDate);
        return m.format('YYYY-MM-DD');
    }

    public static matDateToReadable(matDate: Date | string | null): string {
        if (!matDate) return '';
        const m = typeof matDate === 'string'
            ? moment(matDate, 'YYYY-MM-DD')
            : moment(matDate);
        return m.format('MMMM DD, YYYY');
    }

    public static matDateToTime(matDate: Date | null): string {
        if (!matDate) return '';
        return moment(matDate).format('hh:mm A');
    }

    public static compareActualDate(actualDateParam: any, type: any): boolean {

        try {
            const currentDate = new Date();
            const curDate = new Date(
                currentDate.getFullYear(),
                currentDate.getMonth(),
                currentDate.getDate()
            );

            const actual = new Date(actualDateParam);
            const actDate = new Date(
                actual.getFullYear(),
                actual.getMonth(),
                actual.getDate()
            );

            if (type === HelperService.bankDeposit()) {
                return curDate > actDate;
            }

            if (type === HelperService.check()) {
                return curDate < actDate;
            }

            if (type === HelperService.bill()) {
                return curDate > actDate;
            }

        } catch (e) {

        }
        return false;
    }

    public static dateToday() {

    let format = 'MM/DD/YYYY';
    const dateToday = moment().format(format);

    return moment(dateToday, format).toDate();
  }

  public static dateTomorrow() {

    var today = new Date();
    var tomorrow = new Date();

    let format = 'MM/DD/YYYY';

    tomorrow.setDate(today.getDate()+1);


    const dateTomorrow = moment(tomorrow).format(format);

    return moment(dateTomorrow, format).toDate();
  }

  public static endDateForAweek(date:any) {

    var today = date;
    var tomorrow = new Date();

    let format = 'MM/DD/YYYY';

    tomorrow.setDate(today.getDate()+4);


    const dateTomorrow = moment(tomorrow).format(format);

    return moment(dateTomorrow, format).toDate();
  }

  public static datePlus1(date:any) {

    var today = date;
    var tomorrow = new Date();

    alert(tomorrow);

    let format = 'MM/DD/YYYY';

    tomorrow.setDate(date.getDate()+1);


    const dateTomorrow = moment(tomorrow).format(format);

    return moment(dateTomorrow, format).toDate();
  }


  public static datePlus2(date : any) {

    var today = date;
    var tomorrow = new Date();

    let format = 'MM/DD/YYYY';

    tomorrow.setDate(today.getDate()+2);


    const dateTomorrow = moment(tomorrow).format(format);

    return moment(dateTomorrow, format).toDate();
  }

  public static stringDateToMatDate(stringDate: string): Date {
      return moment(stringDate, 'YYYY-MM-DD').toDate();
  }

  public static addDays(date:any, days: any) {

    var today = date;
    var tomorrow = new Date();

    let format = 'MM/DD/YYYY';

    tomorrow.setDate(today.getDate());

    const dateTomorrow =  moment(date, "MM/DD/YYYY").add('days', days);

    return moment(dateTomorrow, format).toDate();
  }

  public static startOfSelectedMonth(date : any) {

    let format = 'YYYY-MM-DD';
    const startOfMonth = moment(date, "MM/DD/YYYY").startOf('month').format(format);

    return moment(startOfMonth, format).toDate();
  }

  public static endOfSelectedMonth(date : any) {

    let format = 'YYYY-MM-DD';
    const endOfMonth = moment(date, "MM/DD/YYYY").endOf('month').format(format);

    return moment(endOfMonth, format).toDate();
  }

  public static noOfDaysSelectedMonth(date : any) {

    var days = moment(date, 'MM/DD/YYYY').endOf('month');
    return days.format('D');
  }

  public static dateToSql(matDate: Date | string | null): string {
      if (!matDate) return '';
      const dateObj = matDate instanceof Date ? matDate : moment(matDate, 'MMMM D, YYYY').toDate();
      return moment(dateObj).format('YYYY-MM-DD');
  }

    public static isSelectableDueDate(date: Date): boolean {
        if (!date) return false;

        const selectedDate = moment(date).startOf('day');
        const today = moment().startOf('day');

        const isFutureOrToday = selectedDate.isSameOrAfter(today, 'day');
        const isWeekend = selectedDate.day() === 0 || selectedDate.day() === 6;

        return isFutureOrToday && !isWeekend;
    }

    public static parseTimeToDate(timeString: string): Date | null {
        if (!timeString) {
            return null;
        }

        const time = moment(timeString, 'HH:mm:ss', true);
        if (!time.isValid()) {
            return null;
        }

        const today = moment();
        today.set({
            hour: time.hour(),
            minute: time.minute(),
            second: time.second(),
            millisecond: 0
        });

        return today.toDate();
    }

    static timeStringToDate(time: string | null | undefined): Date | null {
        if (!time) {
            return null;
        }

        const parts = time.split(':').map(Number);

        if (parts.length < 2 || parts.some(isNaN)) {
            return null;
        }

        const [hours, minutes, seconds = 0] = parts;

        const date = new Date();
        date.setHours(hours, minutes, seconds, 0);

        return date;
    }

}
