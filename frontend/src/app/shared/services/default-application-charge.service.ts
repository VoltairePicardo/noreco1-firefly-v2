import {inject, Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {environment} from '@/environments/environment';

const API_URL = environment.get('baseApiUrl') + '/default-assessment-charge';
const VAT_RATE = 0.12;

/**
 * Centralized service for managing default assessment charges per assessment type.
 *
 * Assessment type codes match the backend AssessmentType enum:
 *   CHANGE_OF_NAME  – Change of Name applications
 */
@Injectable({providedIn: 'root'})
export class DefaultAssessmentChargeService {

    private http = inject(HttpClient);

    getTypes(): Observable<{code: string; displayName: string}[]> {
        return this.http.get<{code: string; displayName: string}[]>(`${API_URL}/types`);
    }

    getRaw(assessmentType: string): Observable<any[]> {
        return this.http.get<any[]>(`${API_URL}/list/${assessmentType}`);
    }

    /**
     * Returns default charges mapped to the standard applicationCharge shape
     * used across ISD modules (item, quantity, unit, amount, vatable, vat, totalAmount).
     */
    getDefaults(assessmentType: string): Observable<any[]> {
        return this.getRaw(assessmentType).pipe(
            map(charges => charges.map(c => {
                const amount = Number(c.amount) || 0;
                const vatable = !!(c.vatable ?? c.miscItem?.vatType?.id === 1);
                const vat = vatable ? +(amount * VAT_RATE).toFixed(2) : 0;
                return {
                    item: c.miscItem,
                    quantity: 1,
                    unit: null,
                    amount,
                    vatable,
                    vat,
                    totalAmount: +(amount + vat).toFixed(2)
                };
            }))
        );
    }

    save(assessmentType: string, charges: any[]): Observable<any> {
        return this.http.post(`${API_URL}/save/${assessmentType}`, charges);
    }

    create(chargeType: string, charges: any[]): Observable<any> {
        return this.http.post(`${API_URL}/create`, {
            chargeType,
            charges: charges.map(c => ({
                miscItemId: c.miscItem?.id,
                amount: c.amount,
                vatable: c.vatable
            }))
        });
    }

}
